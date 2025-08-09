import React, { useState, useMemo } from 'react';
import {
    Box,
    Container,
    Typography,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    TextField,
    Chip,
    InputAdornment,
    Button,
    Stack,
    Paper,
    useTheme,
    useMediaQuery
} from '@mui/material';
import {
    ExpandMore as ExpandMoreIcon,
    Search as SearchIcon,
    GetApp as GetAppIcon,
    Chat as ChatIcon,
    GitHub as GitHubIcon,
    QuestionMark as QuestionMarkIcon,
    Info as InfoIcon,
    Build as BuildIcon,
    Code as CodeIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Settings as SettingsIcon,
} from '@mui/icons-material';
import type { FAQ } from '../types';
import { Section } from './common';
import Layout from './Layout';
import { Breadcrumbs } from './SEO';

const FAQ_DATA: FAQ[] = [
    {
        id: 'what-is-jcachex',
        question: 'What is JCacheX?',
        answer: 'JCacheX is a high-performance, thread-safe caching library for Java and Kotlin applications. It delivers exceptional performance with intelligent profiles that automatically optimize for your use case. Features include profile-based optimization (501.1M ops/sec with ZeroCopy, 224.6M ops/sec WriteHeavy), comprehensive async support, distributed caching, and seamless Spring Boot integration.',
        category: 'General'
    },
    {
        id: 'why-choose-jcachex',
        question: 'Why choose JCacheX over other caching solutions?',
        answer: 'JCacheX outperforms alternatives with profile-based optimization that eliminates complex configuration. It offers 501.1M ops/sec with ZeroCopy profile, 224.6M ops/sec with WriteHeavy profile, and production-ready features including circuit breakers, monitoring, and resilience patterns. The modern JCacheXBuilder makes cache creation simple while delivering maximum performance.',
        category: 'General'
    },
    {
        id: 'jcachex-builder-patterns',
        question: 'How do I use the new JCacheXBuilder patterns?',
        answer: 'JCacheXBuilder provides modern, profile-based cache creation. Use JCacheXBuilder.forReadHeavyWorkload() for frequent reads, JCacheXBuilder.forWriteHeavyWorkload() for frequent updates, or JCacheXBuilder.forHighPerformance() for ultra-low latency. You can also use JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY) for type-safe profile selection.',
        category: 'Builder Patterns'
    },
    {
        id: 'profile-selection-guide',
        question: 'Which profile should I choose for my use case?',
        answer: 'Profile selection depends on your access patterns: READ_HEAVY (501.1M ops/sec ZeroCopy) for frequently accessed data like user profiles, WRITE_HEAVY (224.6M ops/sec) for frequently updated data like counters, HIGH_PERFORMANCE for ultra-low latency requirements, and MEMORY_EFFICIENT for memory-constrained environments. The builder automatically optimizes internal structures for each profile.',
        category: 'Builder Patterns'
    },
    {
        id: 'one-liner-cache-creation',
        question: 'Can I create caches with one-liners?',
        answer: 'Yes! JCacheXBuilder provides convenience methods for quick cache creation: JCacheXBuilder.createReadHeavyCache("users", 10000) for read-heavy workloads, JCacheXBuilder.createWriteHeavyCache("counters", 5000) for write-heavy workloads, and JCacheXBuilder.createHighPerformanceCache("sessions", 50000) for performance-critical scenarios.',
        category: 'Builder Patterns'
    },
    {
        id: 'migration-from-old-builders',
        question: 'How do I migrate from old builder patterns?',
        answer: 'Migration is straightforward: replace CacheBuilder.newBuilder() with JCacheXBuilder.forReadHeavyWorkload() or appropriate profile. Old patterns like maximumSize(1000L) become maximumSize(1000), and the API remains nearly identical. The new builder automatically applies profile-based optimizations for better performance.',
        category: 'Migration'
    },
    {
        id: 'profile-performance-differences',
        question: 'What are the performance differences between profiles?',
        answer: 'Each profile is optimized for specific patterns: READ_HEAVY delivers 501.1M ops/sec with ZeroCopy implementation, WRITE_HEAVY achieves 224.6M ops/sec with optimized eviction, HIGH_PERFORMANCE provides ultra-low latency with specialized memory layout, and MEMORY_EFFICIENT balances performance with memory usage for constrained environments.',
        category: 'Performance'
    },
    {
        id: 'intelligent-profiles',
        question: 'How do intelligent profiles work?',
        answer: 'Intelligent profiles automatically configure optimal cache settings for your use case. Each profile optimizes internal data structures, memory layout, and algorithms. READ_HEAVY uses ZeroCopy for 501.1M ops/sec, WRITE_HEAVY optimizes eviction for 224.6M ops/sec, API_RESPONSE is perfect for gateway caching, and HIGH_PERFORMANCE achieves ultra-low latency through specialized implementations.',
        category: 'Features'
    },
    {
        id: 'custom-profile-creation',
        question: 'Can I create custom profiles?',
        answer: 'Yes! You can create custom profiles by extending the profile system or using JCacheXBuilder.fromProfile() with custom configurations. Combine profile benefits with specific settings: JCacheXBuilder.fromProfile(ProfileName.READ_HEAVY).maximumSize(50000).expireAfterAccess(Duration.ofMinutes(30)).build() for tailored optimization.',
        category: 'Builder Patterns'
    },
    {
        id: 'profile-based-spring-integration',
        question: 'How do profiles work with Spring Boot integration?',
        answer: 'Spring Boot integration supports profile-based configuration through @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY") annotations. You can define caches in application.yml under the jcachex.caches section (with jcachex.enabled and jcachex.autoCreateCaches), and the auto-configuration creates them and applies defaults. Named caches can specify their profile for automatic optimization.',
        category: 'Integration'
    },
    {
        id: 'profile-monitoring',
        question: 'How do I monitor profile-specific performance?',
        answer: 'JCacheX provides profile-aware monitoring with metrics showing profile-specific performance characteristics. Monitor hit rates, latency, and throughput per profile through JMX, Micrometer, or built-in statistics. Health checks include profile-specific metrics, and you can track how each profile optimizes your specific workload patterns.',
        category: 'Monitoring'
    },
    {
        id: 'when-to-use-profiles',
        question: 'When should I use each profile type?',
        answer: 'Use READ_HEAVY for user profiles, product catalogs, and reference data (frequent reads). Use WRITE_HEAVY for counters, statistics, and frequently updated data. Use HIGH_PERFORMANCE for session management, real-time data, and ultra-low latency requirements. Use MEMORY_EFFICIENT for large datasets in memory-constrained environments.',
        category: 'Builder Patterns'
    },
    {
        id: 'supported-eviction-strategies',
        question: 'What eviction strategies are supported?',
        answer: 'JCacheX supports advanced eviction strategies all optimized to O(1) operations: TinyWindowLFU (recommended hybrid), Enhanced LRU with frequency awareness, Enhanced LFU with buckets, and Zero-Copy LRU for maximum performance. Each profile automatically selects the optimal eviction strategy, though you can override this selection if needed.',
        category: 'Features'
    },
    {
        id: 'async-operations',
        question: 'Does JCacheX support async operations?',
        answer: 'Yes! JCacheX has comprehensive async support with CompletableFuture in Java and coroutines in Kotlin. All profiles support async operations, and you can combine profiles with async patterns: JCacheXBuilder.forReadHeavyWorkload().asyncLoader(this::loadUserAsync).build(). The library includes non-blocking cache warming, async eviction listeners, and reactive patterns.',
        category: 'Features'
    },
    {
        id: 'distributed-caching',
        question: 'Can JCacheX be used for distributed caching?',
        answer: 'Yes, JCacheX supports distributed caching with profile-based optimization. Use profiles for local cache layers in distributed scenarios - READ_HEAVY for frequently accessed distributed data, WRITE_HEAVY for distributed counters, and HIGH_PERFORMANCE for distributed session management. It supports multi-node clustering, eventual consistency, and automatic failover.',
        category: 'Features'
    },
    {
        id: 'spring-boot-integration',
        question: 'How does JCacheX integrate with Spring Boot?',
        answer: 'JCacheX provides Spring Boot integration with auto-configuration. Use @JCacheXCacheable(cacheName = "users", profile = "READ_HEAVY") annotations, and define caches in application.yml (jcachex.enabled=true, jcachex.autoCreateCaches=true). Actuator exposure is optional. Add the jcachex-spring dependency and configure caches for optimal performance.',
        category: 'Integration'
    },
    {
        id: 'monitoring-metrics',
        question: 'What monitoring and metrics are available?',
        answer: 'JCacheX provides comprehensive observability with profile-specific metrics. Monitor READ_HEAVY profile hit rates, WRITE_HEAVY eviction patterns, and HIGH_PERFORMANCE latency metrics. Includes real-time statistics, Prometheus integration, Grafana dashboards, custom event listeners, JMX support, and built-in health indicators with profile-aware monitoring.',
        category: 'Monitoring'
    },
    {
        id: 'thread-safety',
        question: 'Is JCacheX thread-safe?',
        answer: 'Yes, JCacheX is fully thread-safe and optimized for concurrent access across all profiles. Each profile uses specialized concurrency techniques - READ_HEAVY uses lock-free algorithms for maximum throughput, WRITE_HEAVY optimizes for concurrent writes, and HIGH_PERFORMANCE uses advanced concurrency patterns to ensure high performance without compromising data integrity.',
        category: 'Technical'
    },
    {
        id: 'memory-management',
        question: 'How does JCacheX handle memory management?',
        answer: 'JCacheX uses profile-specific memory optimization patterns. READ_HEAVY minimizes allocations with ZeroCopy, WRITE_HEAVY uses efficient eviction to minimize GC pressure, MEMORY_EFFICIENT profile automatically configures optimal memory usage, and HIGH_PERFORMANCE uses specialized memory layout. All profiles support soft references and configurable memory limits.',
        category: 'Technical'
    },
    {
        id: 'serialization-support',
        question: 'What serialization formats are supported?',
        answer: 'JCacheX supports multiple serialization formats with profile-aware optimization. For distributed caching, it handles Java serialization, JSON, Avro, and Protocol Buffers. Each profile optimizes serialization differently - READ_HEAVY minimizes deserialization overhead, WRITE_HEAVY optimizes serialization speed, and HIGH_PERFORMANCE uses the fastest serialization path available.',
        category: 'Technical'
    },
    {
        id: 'configuration-options',
        question: 'What configuration options are available?',
        answer: 'JCacheX offers extensive configuration through profile-based builders. Each profile provides sensible defaults while allowing customization: cache size limits, TTL settings, eviction policies, concurrency levels, and monitoring options. Use JCacheXBuilder.fromProfile() to start with a profile and customize as needed, or use convenience methods for common patterns.',
        category: 'Configuration'
    },
    {
        id: 'performance-comparison',
        question: 'How does JCacheX perform compared to other caching solutions?',
        answer: 'JCacheX delivers industry-leading performance with profile-based optimization: 501.1M ops/sec with ZeroCopy (READ_HEAVY), 224.6M ops/sec with WriteHeavy profile, and ultra-low latency with HIGH_PERFORMANCE profile. These profiles automatically optimize for specific workload patterns, delivering consistent performance advantages over manual configuration approaches.',
        category: 'Performance'
    },
    {
        id: 'migration-guide',
        question: 'How do I migrate from other caching solutions?',
        answer: 'JCacheX provides step-by-step migration guides with profile recommendations. From Caffeine: replace builders with profile-based JCacheXBuilder for automatic optimization. From Redis: use profiles for local caching with 50x+ performance improvement. The API is designed to be familiar, and intelligent profiles often improve performance automatically with minimal code changes.',
        category: 'Migration'
    },
    {
        id: 'troubleshooting',
        question: 'Where can I find troubleshooting help?',
        answer: 'JCacheX provides comprehensive troubleshooting guides with profile-specific diagnostics. Common issues include profile selection guidance, performance optimization tips, and configuration troubleshooting. The diagnostic tools help identify optimal profile choices, and the community provides support through GitHub discussions with profile-specific help.',
        category: 'Support'
    },
    {
        id: 'enterprise-support',
        question: 'Is enterprise support available?',
        answer: 'Yes, enterprise support is available with SLA guarantees, priority support, custom profile development, and training services. Enterprise customers get access to advanced profile features, dedicated support channels, professional services for profile optimization, and custom feature development including specialized profiles for specific use cases.',
        category: 'Support'
    }
];

const FAQPage: React.FC = () => {
    const [openItems, setOpenItems] = useState<Set<string>>(new Set());
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const toggleItem = (id: string) => {
        setOpenItems(prev => {
            const newSet = new Set(prev);
            if (newSet.has(id)) {
                newSet.delete(id);
            } else {
                newSet.add(id);
            }
            return newSet;
        });
    };

    const categories = Array.from(new Set(FAQ_DATA.map(faq => faq.category).filter(Boolean))).sort();

    const filteredFAQs = useMemo(() => {
        return FAQ_DATA.filter(faq => {
            const matchesSearch = faq.question.toLowerCase().includes(searchTerm.toLowerCase()) ||
                faq.answer.toLowerCase().includes(searchTerm.toLowerCase());
            const matchesCategory = selectedCategory === 'all' || faq.category === selectedCategory;
            return matchesSearch && matchesCategory;
        });
    }, [searchTerm, selectedCategory]);

    const navigationItems = [
        {
            id: 'faq-general',
            title: 'General',
            icon: <InfoIcon />,
            children: [
                { id: 'faq-what-is-jcachex', title: 'What is JCacheX?', icon: <InfoIcon /> },
                { id: 'faq-why-choose-jcachex', title: 'Why Choose JCacheX?', icon: <InfoIcon /> },
                { id: 'faq-performance', title: 'Performance', icon: <SpeedIcon /> }
            ]
        },
        {
            id: 'faq-builder',
            title: 'Builder Patterns',
            icon: <BuildIcon />,
            children: [
                { id: 'faq-jcachex-builder', title: 'JCacheXBuilder', icon: <BuildIcon /> },
                { id: 'faq-profile-selection', title: 'Profile Selection', icon: <SettingsIcon /> },
                { id: 'faq-convenience-methods', title: 'Convenience Methods', icon: <CodeIcon /> }
            ]
        },
        {
            id: 'faq-integration',
            title: 'Integration',
            icon: <ExtensionIcon />,
            children: [
                { id: 'faq-spring-boot', title: 'Spring Boot', icon: <ExtensionIcon /> },
                { id: 'faq-kotlin', title: 'Kotlin', icon: <CodeIcon /> },
                { id: 'faq-migration', title: 'Migration', icon: <SettingsIcon /> }
            ]
        },
        {
            id: 'faq-troubleshooting',
            title: 'Troubleshooting',
            icon: <SettingsIcon />,
            children: [
                { id: 'faq-common-issues', title: 'Common Issues', icon: <InfoIcon /> },
                { id: 'faq-performance-tuning', title: 'Performance Tuning', icon: <SpeedIcon /> },
                { id: 'faq-support', title: 'Support', icon: <ChatIcon /> }
            ]
        }
    ];

    const sidebarConfig = {
        title: "FAQ",
        navigationItems: navigationItems,
        expandedByDefault: true
    };

    return (
        <Layout sidebarConfig={sidebarConfig}>
            <Container
                maxWidth={false}
                sx={{
                    py: 4,
                    px: { xs: 2, sm: 3, md: 0 }, // Remove horizontal padding on desktop since Layout handles sidebar offset
                    pr: { xs: 2, sm: 3, md: 4 }, // Keep right padding on desktop
                    pl: { xs: 2, sm: 3, md: 0 }, // Remove left padding on desktop
                    ml: { xs: 0, md: 0 }, // No extra margin on mobile
                    mt: { xs: 1, md: 0 }, // Small top margin on mobile to avoid FAB overlap
                    minHeight: { xs: 'calc(100vh - 80px)', md: 'auto' }, // Ensure full height on mobile
                }}
            >
                <Breadcrumbs
                    items={[
                        { label: 'Home', path: '/' },
                        { label: 'FAQ', path: '/faq', current: true }
                    ]}
                />

                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 6 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
                        <QuestionMarkIcon sx={{ fontSize: 48, color: 'primary.main' }} />
                    </Box>
                    <Typography variant="h2" component="h1" gutterBottom sx={{ fontWeight: 700, mb: 2 }}>
                        Frequently Asked Questions
                    </Typography>
                    <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto', mb: 4 }}>
                        Find answers to common questions about JCacheX. Can't find what you're looking for?
                        Check our documentation or reach out to our community.
                    </Typography>
                </Box>

                {/* Search and Filter */}
                <Paper sx={{ p: 3, mb: 4 }}>
                    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
                        <TextField
                            fullWidth
                            placeholder="Search FAQ..."
                            value={searchTerm}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value)}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon />
                                    </InputAdornment>
                                ),
                            }}
                            sx={{ flex: 1 }}
                        />

                        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
                            <Chip
                                label="All"
                                onClick={() => setSelectedCategory('all')}
                                color={selectedCategory === 'all' ? 'primary' : 'default'}
                                variant={selectedCategory === 'all' ? 'filled' : 'outlined'}
                            />
                            {categories.map((category) => (
                                <Chip
                                    key={category}
                                    label={category}
                                    onClick={() => setSelectedCategory(category as string)}
                                    color={selectedCategory === category ? 'primary' : 'default'}
                                    variant={selectedCategory === category ? 'filled' : 'outlined'}
                                />
                            ))}
                        </Stack>
                    </Stack>

                    <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                        {filteredFAQs.length} {filteredFAQs.length === 1 ? 'result' : 'results'} found
                    </Typography>
                </Paper>

                {/* FAQ Items */}
                <Box sx={{ mb: 6 }}>
                    {filteredFAQs.length === 0 ? (
                        <Paper sx={{ p: 4, textAlign: 'center' }}>
                            <Typography variant="h6" color="text.secondary">
                                No FAQs found matching your search
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                Try adjusting your search terms or browse all categories
                            </Typography>
                        </Paper>
                    ) : (
                        filteredFAQs.map(faq => (
                            <Accordion
                                key={faq.id}
                                expanded={openItems.has(faq.id)}
                                onChange={() => toggleItem(faq.id)}
                                sx={{
                                    mb: 1,
                                    '&:before': { display: 'none' },
                                    boxShadow: 1,
                                    borderRadius: 1,
                                    '&.Mui-expanded': {
                                        margin: '0 0 8px 0',
                                    }
                                }}
                            >
                                <AccordionSummary
                                    expandIcon={<ExpandMoreIcon />}
                                    sx={{
                                        bgcolor: 'grey.50',
                                        '&.Mui-expanded': {
                                            borderBottom: `1px solid ${theme.palette.divider}`,
                                        },
                                        minHeight: isMobile ? 56 : 64,
                                        px: 3,
                                        py: 2
                                    }}
                                >
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, width: '100%' }}>
                                        <Typography variant="h6" component="h2" sx={{
                                            flex: 1,
                                            fontSize: isMobile ? '1rem' : '1.25rem',
                                            lineHeight: 1.3
                                        }}>
                                            {faq.question}
                                        </Typography>
                                        <Chip
                                            label={faq.category}
                                            size="small"
                                            color="primary"
                                            variant="outlined"
                                            sx={{ ml: 'auto' }}
                                        />
                                    </Box>
                                </AccordionSummary>
                                <AccordionDetails sx={{ p: 3 }}>
                                    <Typography variant="body1" sx={{
                                        lineHeight: 1.7,
                                        fontSize: isMobile ? '0.9rem' : '1rem'
                                    }}>
                                        {faq.answer}
                                    </Typography>

                                    {/* Action buttons for mobile-friendly experience */}
                                    <Box sx={{
                                        display: 'flex',
                                        gap: 1,
                                        mt: 2,
                                        flexDirection: isMobile ? 'column' : 'row'
                                    }}>
                                        <Button
                                            variant="outlined"
                                            size="small"
                                            startIcon={<GetAppIcon />}
                                            href="/getting-started"
                                            sx={{ textTransform: 'none' }}
                                        >
                                            Get Started
                                        </Button>
                                        <Button
                                            variant="outlined"
                                            size="small"
                                            startIcon={<GitHubIcon />}
                                            href="https://github.com/dhruv1110/jcachex"
                                            target="_blank"
                                            sx={{ textTransform: 'none' }}
                                        >
                                            View on GitHub
                                        </Button>
                                    </Box>
                                </AccordionDetails>
                            </Accordion>
                        ))
                    )}
                </Box>

                {/* Help Section */}
                <Paper sx={{ p: 4, textAlign: 'center', bgcolor: 'primary.main', color: 'white' }}>
                    <Typography variant="h4" gutterBottom>
                        Still need help?
                    </Typography>
                    <Typography variant="body1" sx={{ mb: 3, opacity: 0.9 }}>
                        Our community and documentation are here to help you succeed with JCacheX.
                    </Typography>
                    <Stack
                        direction={{ xs: 'column', sm: 'row' }}
                        spacing={2}
                        justifyContent="center"
                        sx={{ maxWidth: 500, mx: 'auto' }}
                    >
                        <Button
                            variant="contained"
                            size="large"
                            href="/documentation"
                            sx={{
                                bgcolor: 'white',
                                color: 'primary.main',
                                '&:hover': { bgcolor: 'grey.100' },
                                textTransform: 'none',
                                minWidth: isMobile ? '100%' : 'auto'
                            }}
                        >
                            View Documentation
                        </Button>
                        <Button
                            variant="outlined"
                            size="large"
                            startIcon={<ChatIcon />}
                            href="https://github.com/dhruv1110/jcachex/discussions"
                            target="_blank"
                            sx={{
                                borderColor: 'white',
                                color: 'white',
                                '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                                textTransform: 'none',
                                minWidth: isMobile ? '100%' : 'auto'
                            }}
                        >
                            Join Discussions
                        </Button>
                        <Button
                            variant="outlined"
                            size="large"
                            startIcon={<GitHubIcon />}
                            href="https://github.com/dhruv1110/jcachex/issues/new"
                            target="_blank"
                            sx={{
                                borderColor: 'white',
                                color: 'white',
                                '&:hover': { bgcolor: 'rgba(255,255,255,0.1)' },
                                textTransform: 'none',
                                minWidth: isMobile ? '100%' : 'auto'
                            }}
                        >
                            Report Issue
                        </Button>
                    </Stack>
                </Paper>
            </Container>
        </Layout>
    );
};

export default FAQPage;
