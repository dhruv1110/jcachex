package io.github.dhruv1110.jcachex.spring.aop;

import io.github.dhruv1110.jcachex.Cache;
import io.github.dhruv1110.jcachex.JCacheXBuilder;
import io.github.dhruv1110.jcachex.profiles.CacheProfile;
import io.github.dhruv1110.jcachex.profiles.ProfileRegistry;
import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheEvict;
import io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheable;
import io.github.dhruv1110.jcachex.spring.core.JCacheXCacheManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class JCacheXAnnotationsAspect {

    private final JCacheXCacheManager cacheManager;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final BeanFactory beanFactory;

    @Autowired
    public JCacheXAnnotationsAspect(JCacheXCacheManager cacheManager, BeanFactory beanFactory) {
        this.cacheManager = cacheManager;
        this.beanFactory = beanFactory;
    }

    @Around("@annotation(io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheable)")
    public Object aroundCacheable(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        JCacheXCacheable ann = method.getAnnotation(JCacheXCacheable.class);
        String cacheName = ann.cacheName().isEmpty() ? method.getDeclaringClass().getName() + "." + method.getName()
                : ann.cacheName();

        Object key = generateKey(ann.key(), method, pjp.getArgs(), pjp.getTarget());

        Cache<Object, Object> cache = cacheManager.getNativeCache(cacheName);
        if (cache == null) {
            cache = buildCache(cacheName, ann);
            cacheManager.registerCache(cacheName, cache);
        }

        Object cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Object result = pjp.proceed();
        cache.put(key, result);
        return result;
    }

    @Around("@annotation(io.github.dhruv1110.jcachex.spring.annotations.JCacheXCacheEvict)")
    public Object aroundEvict(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        JCacheXCacheEvict ann = method.getAnnotation(JCacheXCacheEvict.class);
        String cacheName = ann.cacheName().isEmpty() ? method.getDeclaringClass().getName() + "." + method.getName()
                : ann.cacheName();
        Cache<Object, Object> cache = cacheManager.getNativeCache(cacheName);
        Object key = generateKey(ann.key(), method, pjp.getArgs(), pjp.getTarget());

        if (ann.beforeInvocation()) {
            if (cache != null) {
                if (ann.allEntries()) {
                    cache.clear();
                } else if (key != null) {
                    cache.remove(key);
                }
            }
            return pjp.proceed();
        } else {
            Object result = pjp.proceed();
            if (cache != null) {
                if (ann.allEntries()) {
                    cache.clear();
                } else if (key != null) {
                    cache.remove(key);
                }
            }
            return result;
        }
    }

    private Cache<Object, Object> buildCache(String cacheName, JCacheXCacheable ann) {
        JCacheXBuilder<Object, Object> builder;
        String profileName = ann.profile();
        if (profileName != null && !profileName.trim().isEmpty()) {
            CacheProfile<?, ?> profile = ProfileRegistry.getProfile(profileName.toUpperCase());
            builder = JCacheXBuilder.forProfile(profile);
        } else {
            builder = JCacheXBuilder.withSmartDefaults();
        }
        builder.name(cacheName);
        if (ann.maximumSize() > 0) {
            builder.maximumSize(ann.maximumSize());
        }
        if (ann.expireAfterWrite() > 0) {
            long seconds = TimeUnit.SECONDS.convert(ann.expireAfterWrite(), ann.expireAfterWriteUnit());
            builder.expireAfterWrite(Duration.ofSeconds(seconds));
        }
        builder.recordStats(false);
        return builder.build();
    }

    private Object generateKey(String expr, Method method, Object[] args, Object target) {
        if (expr == null || expr.trim().isEmpty()) {
            return args.length == 1 ? args[0] : java.util.Arrays.deepHashCode(args);
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("args", args);
        context.setVariable("method", method);
        context.setVariable("target", target);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        Expression expression = parser.parseExpression(expr);
        return expression.getValue(context);
    }
}
