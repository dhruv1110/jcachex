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

// Shared styles
const linkStyles = {
    color: 'grey.400',
    textDecoration: 'none',
    '&:hover': { color: 'primary.main' },
    transition: 'color 0.2s ease',
} as const;

// Reusable link components
const FooterLink: React.FC<{ to: string; children: React.ReactNode }> = ({ to, children }) => (
    <Typography
        variant="body2"
        component={Link}
        to={to}
        sx={linkStyles}
    >
        {children}
    </Typography>
);

const ExternalFooterLink: React.FC<{ href: string; children: React.ReactNode }> = ({ href, children }) => (
    <Typography
        variant="body2"
        component="a"
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        sx={linkStyles}
    >
        {children}
    </Typography>
);

// Navigation data
const navigationSections = [
    {
        title: 'Documentation',
        links: [
            { type: 'internal', to: '/getting-started', label: 'Getting Started' },
            { type: 'internal', to: '/examples', label: 'Examples' },
            { type: 'internal', to: '/spring', label: 'Spring Guide' },
            { type: 'internal', to: '/faq', label: 'FAQ' },
        ]
    },
    {
        title: 'Resources',
        links: [
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX', label: 'GitHub Repository' },
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX/releases', label: 'Releases' },
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX/issues', label: 'Report Issues' },
        ]
    },
    {
        title: 'Community',
        links: [
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX/blob/main/CONTRIBUTING.md', label: 'Contributing' },
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX/blob/main/CODE_OF_CONDUCT.md', label: 'Code of Conduct' },
            { type: 'external', href: 'https://github.com/dhruv1110/JCacheX/blob/main/LICENSE', label: 'License' },
        ]
    }
] as const;

const Footer: React.FC = () => {
    const currentYear = new Date().getFullYear();
    const theme = useTheme();

    return (
        <Box
            component="footer"
            sx={{
                bgcolor: 'var(--jcx-surface)',
                color: 'var(--jcx-text-primary)',
                mt: 'auto',
                py: 6,
                zIndex: 10000,
                borderTop: '1px solid var(--jcx-divider)'
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
                                    bgcolor: 'rgba(255,255,255,0.06)',
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
                        {navigationSections.map((section) => (
                            <Box key={section.title} sx={{ flex: 1 }}>
                                <Typography variant="h6" color="grey.100" gutterBottom>
                                    {section.title}
                                </Typography>
                                <Stack spacing={0.5}>
                                    {section.links.map((link) =>
                                        link.type === 'internal' ? (
                                            <FooterLink key={link.label} to={link.to!}>
                                                {link.label}
                                            </FooterLink>
                                        ) : (
                                            <ExternalFooterLink key={link.label} href={link.href!}>
                                                {link.label}
                                            </ExternalFooterLink>
                                        )
                                    )}
                                </Stack>
                            </Box>
                        ))}
                    </Box>
                </Box>

                {/* Footer Bottom */}
                <Divider sx={{ my: 3, borderColor: 'var(--jcx-divider)' }} />
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
                    <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                        <Typography variant="body2" color="grey.400">
                        Built with ❤️ for the Java community
                        </Typography>
                    </Box>
                </Box>
            </Container>
        </Box>
    );
};

export default Footer;
