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
    QuestionMark as QuestionMarkIcon
} from '@mui/icons-material';
import type { FAQ } from '../types';
import { Section } from './common';
import PageWrapper from './PageWrapper';
import { Breadcrumbs } from './SEO';
import './FAQ.css';

const FAQ_DATA: FAQ[] = [
    {
        id: 'what-is-jcachex',
        question: 'What is JCacheX?',
        answer: 'JCacheX is a high-performance, thread-safe caching library for Java and Kotlin applications. It delivers 7.9ns GET operations with intelligent profiles that automatically optimize for your use case. Features include zero-copy operations, comprehensive async support, distributed caching, and seamless Spring Boot integration.',
        category: 'General'
    },
    {
        id: 'why-choose-jcachex',
        question: 'Why choose JCacheX over other caching solutions?',
        answer: 'JCacheX outperforms alternatives with 3x faster operations than Caffeine and 50x lower latency than Redis. It offers intelligent profiles that eliminate complex configuration, zero-copy operations for maximum performance, and production-ready features including circuit breakers, monitoring, and resilience patterns.',
        category: 'General'
    },
    {
        id: 'intelligent-profiles',
        question: 'How do intelligent profiles work?',
        answer: 'Intelligent profiles automatically configure optimal cache settings for your use case. READ_HEAVY profile delivers 11.5ns GET performance, WRITE_HEAVY optimizes for mutations, API_RESPONSE is perfect for gateway caching, and ZERO_COPY achieves 7.9ns ultra-low latency. No complex configuration needed!',
        category: 'Features'
    },
    {
        id: 'supported-eviction-strategies',
        question: 'What eviction strategies are supported?',
        answer: 'JCacheX supports advanced eviction strategies all optimized to O(1) operations: TinyWindowLFU (recommended hybrid), Enhanced LRU with frequency awareness, Enhanced LFU with buckets, and Zero-Copy LRU for maximum performance. All strategies use frequency sketches for improved accuracy.',
        category: 'Features'
    },
    {
        id: 'async-operations',
        question: 'Does JCacheX support async operations?',
        answer: 'Yes! JCacheX has comprehensive async support with CompletableFuture in Java and coroutines in Kotlin. All operations can be performed asynchronously without blocking threads. The library includes non-blocking cache warming, async eviction listeners, and reactive patterns.',
        category: 'Features'
    },
    {
        id: 'distributed-caching',
        question: 'Can JCacheX be used for distributed caching?',
        answer: 'Yes, JCacheX supports distributed caching with multi-node clustering, eventual consistency, automatic failover, and network partitioning tolerance. It uses efficient serialization for network communication and provides configurable consistency levels.',
        category: 'Features'
    },
    {
        id: 'spring-boot-integration',
        question: 'How does JCacheX integrate with Spring Boot?',
        answer: 'JCacheX provides seamless Spring Boot integration with auto-configuration, @JCacheXCacheable and @JCacheXCacheEvict annotations, actuator endpoints for monitoring, and automatic health checks. Simply add the starter dependency and configure profiles in application.yml.',
        category: 'Integration'
    },
    {
        id: 'monitoring-metrics',
        question: 'What monitoring and metrics are available?',
        answer: 'JCacheX provides comprehensive observability with real-time statistics, Prometheus integration, Grafana dashboards, custom event listeners, JMX support, and built-in health indicators. Monitor hit rates, latency, evictions, and memory usage out of the box.',
        category: 'Monitoring'
    },
    {
        id: 'thread-safety',
        question: 'Is JCacheX thread-safe?',
        answer: 'Yes, JCacheX is fully thread-safe and optimized for concurrent access. It uses lock-free algorithms and advanced concurrency techniques to ensure high performance in multi-threaded environments without compromising data integrity.',
        category: 'Technical'
    },
    {
        id: 'memory-management',
        question: 'How does JCacheX handle memory management?',
        answer: 'JCacheX uses optimized allocation patterns to minimize GC pressure, supports soft references for memory-constrained environments, and provides configurable memory limits. The MEMORY_EFFICIENT profile automatically configures optimal memory usage patterns.',
        category: 'Technical'
    },
    {
        id: 'serialization-support',
        question: 'What serialization formats are supported?',
        answer: 'JCacheX supports multiple serialization formats including Java serialization, JSON, Avro, and Protocol Buffers. For distributed caching, it automatically handles serialization/deserialization with configurable compression and encryption options.',
        category: 'Technical'
    },
    {
        id: 'configuration-options',
        question: 'What configuration options are available?',
        answer: 'JCacheX offers extensive configuration options including cache size limits, TTL settings, eviction policies, concurrency levels, and monitoring options. Intelligent profiles provide pre-configured settings, while advanced users can customize every aspect of cache behavior.',
        category: 'Configuration'
    },
    {
        id: 'performance-comparison',
        question: 'How does JCacheX perform compared to other caching solutions?',
        answer: 'JCacheX delivers industry-leading performance with 7.9ns GET operations (3x faster than Caffeine), 50M+ operations per second throughput, and 50x lower latency than Redis. Comprehensive benchmarks show consistent performance advantages across all workload types.',
        category: 'Performance'
    },
    {
        id: 'migration-guide',
        question: 'How do I migrate from other caching solutions?',
        answer: 'JCacheX provides step-by-step migration guides for popular solutions like Caffeine, Redis, and Guava. The API is designed to be familiar, and intelligent profiles often improve performance automatically. Migration tools and compatibility layers are available.',
        category: 'Migration'
    },
    {
        id: 'troubleshooting',
        question: 'Where can I find troubleshooting help?',
        answer: 'JCacheX provides comprehensive troubleshooting guides, diagnostic tools, and monitoring dashboards. Common issues are documented with solutions, and the community provides support through GitHub discussions and issues.',
        category: 'Support'
    },
    {
        id: 'enterprise-support',
        question: 'Is enterprise support available?',
        answer: 'Yes, enterprise support is available with SLA guarantees, priority support, custom feature development, and training services. Enterprise customers get access to advanced features, dedicated support channels, and professional services.',
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

    const categories = Array.from(new Set(FAQ_DATA.map(faq => faq.category))).sort();

    const filteredFAQs = useMemo(() => {
        return FAQ_DATA.filter(faq => {
            const matchesSearch = faq.question.toLowerCase().includes(searchTerm.toLowerCase()) ||
                faq.answer.toLowerCase().includes(searchTerm.toLowerCase());
            const matchesCategory = selectedCategory === 'all' || faq.category === selectedCategory;
            return matchesSearch && matchesCategory;
        });
    }, [searchTerm, selectedCategory]);

    return (
        <PageWrapper>
            <Container maxWidth="lg" sx={{ py: 4 }}>
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
                    <Typography variant="h1" component="h1" gutterBottom>
                        Frequently Asked Questions
                    </Typography>
                    <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 600, mx: 'auto' }}>
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
                            {categories.map(category => (
                                <Chip
                                    key={category}
                                    label={category}
                                    onClick={() => setSelectedCategory(category)}
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
        </PageWrapper>
    );
};

export default FAQPage;
