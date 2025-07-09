import React, { useState } from 'react';
import type { FAQ } from '../types';
import { Section } from './common';
import PageWrapper from './PageWrapper';
import './FAQ.css';


const FAQ_DATA: FAQ[] = [
    {
        id: 'what-is-jcachex',
        question: 'What is JCacheX?',
        answer: 'JCacheX is a high-performance, thread-safe caching library for Java and Kotlin applications. It provides various eviction strategies, async support, distributed caching, and Spring Boot integration.',
        category: 'General'
    },
    {
        id: 'why-choose-jcachex',
        question: 'Why choose JCacheX over other caching solutions?',
        answer: 'JCacheX offers superior performance with zero-copy operations, comprehensive async support, flexible eviction strategies, built-in monitoring, and seamless Spring Boot integration. It\'s designed for modern cloud-native applications.',
        category: 'General'
    },
    {
        id: 'supported-eviction-strategies',
        question: 'What eviction strategies are supported?',
        answer: 'JCacheX supports LRU (Least Recently Used), LFU (Least Frequently Used), FIFO (First In, First Out), LIFO (Last In, First Out), Time-based, Weight-based, and custom eviction strategies.',
        category: 'Features'
    },
    {
        id: 'async-operations',
        question: 'Does JCacheX support async operations?',
        answer: 'Yes! JCacheX has full async support with CompletableFuture in Java and coroutines in Kotlin. All operations can be performed asynchronously without blocking threads.',
        category: 'Features'
    },
    {
        id: 'distributed-caching',
        question: 'Can JCacheX be used for distributed caching?',
        answer: 'Yes, JCacheX supports distributed caching with configurable consistency levels, automatic failover, and network partitioning handling. It uses efficient serialization for network communication.',
        category: 'Features'
    },
    {
        id: 'spring-boot-integration',
        question: 'How does JCacheX integrate with Spring Boot?',
        answer: 'JCacheX provides seamless Spring Boot integration with auto-configuration, annotations (@JCacheXCacheable, @JCacheXCacheEvict), and automatic health checks. Simply add the starter dependency.',
        category: 'Integration'
    },
    {
        id: 'monitoring-metrics',
        question: 'What monitoring and metrics are available?',
        answer: 'JCacheX provides comprehensive metrics including hit/miss ratios, eviction counts, memory usage, and performance metrics. It integrates with Micrometer for Spring Boot applications.',
        category: 'Monitoring'
    },
    {
        id: 'thread-safety',
        question: 'Is JCacheX thread-safe?',
        answer: 'Yes, JCacheX is fully thread-safe and designed for high-concurrency environments. It uses lock-free data structures and optimistic locking for maximum performance.',
        category: 'Performance'
    },
    {
        id: 'memory-management',
        question: 'How does JCacheX handle memory management?',
        answer: 'JCacheX implements smart memory management with configurable limits, automatic cleanup, and efficient memory usage monitoring. You can set memory limits by size or weight.',
        category: 'Performance'
    },
    {
        id: 'serialization-support',
        question: 'What serialization formats are supported?',
        answer: 'JCacheX supports Java serialization, JSON, Kryo, and custom serialization. For distributed caching, you can choose the most efficient serialization method for your use case.',
        category: 'Configuration'
    },
    {
        id: 'configuration-options',
        question: 'What configuration options are available?',
        answer: 'JCacheX is highly configurable with options for cache size, eviction policies, expiration times, persistence, networking, and more. Configuration can be done via code, properties, or YAML.',
        category: 'Configuration'
    },
    {
        id: 'performance-benchmarks',
        question: 'How does JCacheX perform compared to other caching solutions?',
        answer: 'JCacheX consistently outperforms other caching solutions with 2-3x better throughput, lower latency, and better memory efficiency. Check our benchmarks page for detailed comparisons.',
        category: 'Performance'
    },
    {
        id: 'migration-guide',
        question: 'How do I migrate from other caching solutions?',
        answer: 'We provide migration guides for popular caching solutions like Caffeine, Guava Cache, and Ehcache. Most migrations can be done with minimal code changes.',
        category: 'Migration'
    },
    {
        id: 'troubleshooting',
        question: 'Where can I find troubleshooting help?',
        answer: 'Check our troubleshooting guide, API documentation, and GitHub issues. For immediate help, join our community discussions or create a GitHub issue.',
        category: 'Support'
    },
    {
        id: 'enterprise-support',
        question: 'Is enterprise support available?',
        answer: 'Yes, we offer enterprise support with SLA guarantees, priority issue resolution, and custom feature development. Contact us for enterprise licensing and support options.',
        category: 'Support'
    }
];

const FAQ_CATEGORIES = ['All', 'General', 'Features', 'Integration', 'Monitoring', 'Performance', 'Configuration', 'Migration', 'Support'];

interface FAQItemProps {
    faq: FAQ;
    isOpen: boolean;
    onToggle: () => void;
}

const FAQItem: React.FC<FAQItemProps> = ({ faq, isOpen, onToggle }) => {
    return (
        <div className={`faq-item ${isOpen ? 'open' : ''}`}>
            <button
                className="faq-question"
                onClick={onToggle}
                aria-expanded={isOpen}
                aria-controls={`faq-answer-${faq.id}`}
            >
                <span className="faq-question-text">{faq.question}</span>
                <svg
                    className={`faq-icon ${isOpen ? 'open' : ''}`}
                    width="16"
                    height="16"
                    viewBox="0 0 16 16"
                    fill="none"
                    xmlns="http://www.w3.org/2000/svg"
                >
                    <path
                        d="M4 6L8 10L12 6"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                    />
                </svg>
            </button>
            <div
                id={`faq-answer-${faq.id}`}
                className="faq-answer"
                role="region"
                aria-labelledby={`faq-question-${faq.id}`}
            >
                <div className="faq-answer-content">
                    <p>{faq.answer}</p>
                </div>
            </div>
        </div>
    );
};

const FAQPage: React.FC = () => {
    const [openItems, setOpenItems] = useState<Set<string>>(new Set());
    const [selectedCategory, setSelectedCategory] = useState<string>('All');
    const [searchTerm, setSearchTerm] = useState<string>('');

    const toggleItem = (id: string) => {
        const newOpenItems = new Set(openItems);
        if (newOpenItems.has(id)) {
            newOpenItems.delete(id);
        } else {
            newOpenItems.add(id);
        }
        setOpenItems(newOpenItems);
    };

    const filteredFAQs = FAQ_DATA.filter(faq => {
        const matchesCategory = selectedCategory === 'All' || faq.category === selectedCategory;
        const matchesSearch = faq.question.toLowerCase().includes(searchTerm.toLowerCase()) ||
            faq.answer.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesCategory && matchesSearch;
    });

    return (
        <PageWrapper
            title="Frequently Asked Questions - JCacheX"
            description="Find answers to common questions about JCacheX Java caching framework, including usage, performance, configuration, and troubleshooting."
            keywords="JCacheX, FAQ, questions, answers, help, support, Java cache"
            className="faq-page"
        >

            {/* Header */}
            <Section background="gradient" padding="lg" centered>
                <div className="faq-header">
                    <h1 className="faq-title">Frequently Asked Questions</h1>
                    <p className="faq-subtitle">
                        Find answers to common questions about JCacheX. Can't find what you're looking for?
                        Check our documentation or reach out to our community.
                    </p>
                </div>
            </Section>

            {/* Search and Filter */}
            <Section background="dark" padding="lg">
                <div className="faq-controls">
                    <div className="faq-search">
                        <input
                            type="text"
                            placeholder="Search FAQ..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="faq-search-input"
                        />
                        <svg className="faq-search-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
                            <path
                                d="M9 17A8 8 0 1 0 9 1a8 8 0 0 0 0 16zM19 19l-4.35-4.35"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                    </div>

                    <div className="faq-categories">
                        {FAQ_CATEGORIES.map(category => (
                            <button
                                key={category}
                                className={`faq-category-btn ${selectedCategory === category ? 'active' : ''}`}
                                onClick={() => setSelectedCategory(category)}
                            >
                                {category}
                            </button>
                        ))}
                    </div>
                </div>
            </Section>

            {/* FAQ Items */}
            <Section background="default" padding="lg">
                <div className="faq-content">
                    {filteredFAQs.length > 0 ? (
                        <div className="faq-list">
                            {filteredFAQs.map(faq => (
                                <FAQItem
                                    key={faq.id}
                                    faq={faq}
                                    isOpen={openItems.has(faq.id)}
                                    onToggle={() => toggleItem(faq.id)}
                                />
                            ))}
                        </div>
                    ) : (
                        <div className="faq-empty">
                            <div className="faq-empty-icon">🔍</div>
                            <h3>No results found</h3>
                            <p>Try adjusting your search or filter criteria.</p>
                        </div>
                    )}
                </div>
            </Section>

            {/* Footer CTA */}
            <Section background="gradient" padding="lg" centered>
                <div className="faq-footer">
                    <h3>Still need help?</h3>
                    <p>
                        Our community and documentation are here to help you succeed with JCacheX.
                    </p>
                    <div className="faq-footer-buttons">
                        <a href="/getting-started" className="btn btn-primary">
                            View Documentation
                        </a>
                        <a href="https://github.com/dhruv1110/JCacheX/discussions" className="btn btn-secondary">
                            Join Discussions
                        </a>
                        <a href="https://github.com/dhruv1110/JCacheX/issues" className="btn btn-outline">
                            Report Issue
                        </a>
                    </div>
                </div>
            </Section>
        </PageWrapper>
    );
};

export default FAQPage;
