import React from 'react';
import { Link } from 'react-router-dom';
import { Container, Typography, Button, Box, Paper } from '@mui/material';
import { Home as HomeIcon, Search as SearchIcon, Help as HelpIcon } from '@mui/icons-material';
import { MetaTags, Breadcrumbs } from './SEO';
import Layout from './Layout';

const NotFoundPage: React.FC = () => {
    const seoData = {
        title: '404 - Page Not Found | JCacheX',
        description: 'Sorry, the page you are looking for could not be found. Return to JCacheX homepage or browse our documentation.',
        keywords: ['404', 'not found', 'error', 'JCacheX'],
        canonical: 'https://dhruv1110.github.io/jcachex/404',
        type: 'website' as const
    };

    return (
        <Layout>
            <MetaTags seo={seoData} />

            <Container maxWidth="md" sx={{ py: 8 }}>
                <Paper
                    sx={{
                        p: { xs: 4, md: 6 },
                        textAlign: 'center',
                        backgroundColor: 'background.paper',
                        borderRadius: 3,
                        boxShadow: 3
                    }}
                >
                    <Box sx={{ mb: 4 }}>
                        <Typography
                            variant="h1"
                            component="h1"
                            sx={{
                                fontSize: { xs: '4rem', md: '6rem' },
                                fontWeight: 700,
                                color: 'primary.main',
                                mb: 2
                            }}
                        >
                            404
                        </Typography>
                        <Typography
                            variant="h4"
                            component="h2"
                            sx={{
                                mb: 2,
                                fontWeight: 600,
                                color: 'text.primary'
                            }}
                        >
                            Page Not Found
                        </Typography>
                        <Typography
                            variant="body1"
                            sx={{
                                color: 'text.secondary',
                                fontSize: '1.1rem',
                                lineHeight: 1.6,
                                mb: 4,
                                maxWidth: '600px',
                                mx: 'auto'
                            }}
                        >
                            The page you are looking for might have been removed, had its name changed,
                            or is temporarily unavailable. Let's get you back on track.
                        </Typography>
                    </Box>

                    <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'wrap' }}>
                        <Button
                            component={Link}
                            to="/"
                            variant="contained"
                            size="large"
                            startIcon={<HomeIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            Go Home
                        </Button>
                        <Button
                            component={Link}
                            to="/getting-started"
                            variant="outlined"
                            size="large"
                            startIcon={<SearchIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            Browse Documentation
                        </Button>
                        <Button
                            component={Link}
                            to="/faq"
                            variant="outlined"
                            size="large"
                            startIcon={<HelpIcon />}
                            sx={{ px: 4, py: 1.5 }}
                        >
                            View FAQ
                        </Button>
                    </Box>

                    <Box sx={{ mt: 6, pt: 4, borderTop: 1, borderColor: 'divider' }}>
                        <Typography variant="body2" color="text.secondary">
                            If you believe this is an error, please{' '}
                            <Button
                                component="a"
                                href="https://github.com/dhruv1110/JCacheX/issues"
                                target="_blank"
                                rel="noopener noreferrer"
                                variant="text"
                                size="small"
                                sx={{ textDecoration: 'underline' }}
                            >
                                report it on GitHub
                            </Button>
                        </Typography>
                    </Box>
                </Paper>
            </Container>
        </Layout>
    );
};

export default NotFoundPage;
