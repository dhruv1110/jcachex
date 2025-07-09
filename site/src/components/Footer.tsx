import React from 'react';
import { Link } from 'react-router-dom';
import {
    Box,
    Container,
    Typography,
    IconButton,
    Stack,
    Divider,
    useTheme,
} from '@mui/material';
import { GitHub as GitHubIcon } from '@mui/icons-material';

const Footer: React.FC = () => {
    const currentYear = new Date().getFullYear();
    const theme = useTheme();

    return (
        <Box
            component="footer"
            sx={{
                bgcolor: 'grey.900',
                color: 'grey.100',
                mt: 'auto',
                py: 6,
            }}
        >
            <Container maxWidth="lg">
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: { xs: 'column', md: 'row' },
                        gap: 4,
                    }}
                >
                    {/* Brand Section */}
                    <Box sx={{ flex: { md: '0 0 33%' } }}>
                        <Typography
                            variant="h6"
                            component={Link}
                            to="/"
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 1,
                                color: 'primary.main',
                                textDecoration: 'none',
                                fontWeight: 700,
                                mb: 2,
                            }}
                        >
                            <Box
                                component="svg"
                                sx={{ width: 32, height: 32, fill: 'currentColor' }}
                                viewBox="0 0 24 24"
                            >
                                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                            </Box>
                            JCacheX
                        </Typography>
                        <Typography variant="body2" color="grey.400" sx={{ mb: 2, maxWidth: 280 }}>
                            A high-performance, open source Java caching framework with distributed support,
                            intelligent eviction strategies, and seamless Spring Boot integration.
                        </Typography>
                        <Box>
                            <IconButton
                                href="https://github.com/dhruv1110/JCacheX"
                                target="_blank"
                                rel="noopener noreferrer"
                                sx={{
                                    color: 'grey.400',
                                    bgcolor: 'grey.800',
                                    '&:hover': {
                                        bgcolor: 'primary.main',
                                        color: 'white',
                                        transform: 'translateY(-2px)',
                                    },
                                    transition: 'all 0.2s ease',
                                }}
                            >
                                <GitHubIcon />
                            </IconButton>
                        </Box>
                    </Box>

                    {/* Navigation Links */}
                    <Box
                        sx={{
                            flex: 1,
                            display: 'flex',
                            flexDirection: { xs: 'column', sm: 'row' },
                            gap: 4,
                        }}
                    >
                        <Box sx={{ flex: 1 }}>
                            <Typography variant="h6" color="grey.100" gutterBottom>
                                Documentation
                            </Typography>
                            <Stack spacing={0.5}>
                                <Typography
                                    variant="body2"
                                    component={Link}
                                    to="/getting-started"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Getting Started
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component={Link}
                                    to="/examples"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Examples
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component={Link}
                                    to="/spring"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Spring Guide
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component={Link}
                                    to="/faq"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    FAQ
                                </Typography>
                            </Stack>
                        </Box>

                        <Box sx={{ flex: 1 }}>
                            <Typography variant="h6" color="grey.100" gutterBottom>
                                Resources
                            </Typography>
                            <Stack spacing={0.5}>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    GitHub Repository
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/releases"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Releases
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/issues"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Report Issues
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/discussions"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Discussions
                                </Typography>
                            </Stack>
                        </Box>

                        <Box sx={{ flex: 1 }}>
                            <Typography variant="h6" color="grey.100" gutterBottom>
                                Community
                            </Typography>
                            <Stack spacing={0.5}>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/blob/main/CONTRIBUTING.md"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Contributing
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/blob/main/CODE_OF_CONDUCT.md"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    Code of Conduct
                                </Typography>
                                <Typography
                                    variant="body2"
                                    component="a"
                                    href="https://github.com/dhruv1110/JCacheX/blob/main/LICENSE"
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{
                                        color: 'grey.400',
                                        textDecoration: 'none',
                                        '&:hover': { color: 'primary.main' },
                                        transition: 'color 0.2s ease',
                                    }}
                                >
                                    License
                                </Typography>
                            </Stack>
                        </Box>
                    </Box>
                </Box>

                {/* Footer Bottom */}
                <Divider sx={{ my: 3, borderColor: 'grey.800' }} />
                <Box
                    sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flexDirection: { xs: 'column', sm: 'row' },
                        gap: 1,
                    }}
                >
                    <Typography variant="body2" color="grey.400">
                        © {currentYear} JCacheX. Released under the MIT License.
                    </Typography>
                    <Typography variant="body2" color="grey.400">
                        Built with ❤️ for the Java community
                    </Typography>
                </Box>
            </Container>
        </Box>
    );
};

export default Footer;
