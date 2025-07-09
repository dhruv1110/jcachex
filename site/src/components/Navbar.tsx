import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
    AppBar,
    Toolbar,
    Typography,
    Button,
    IconButton,
    Box,
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemText,
    useTheme,
    useMediaQuery,
} from '@mui/material';
import {
    Menu as MenuIcon,
    Close as CloseIcon,
    GitHub as GitHubIcon,
} from '@mui/icons-material';

const Navbar: React.FC = () => {
    const [mobileOpen, setMobileOpen] = useState(false);
    const location = useLocation();
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    const isActiveLink = (path: string) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    const navigationLinks = [
        { label: 'Docs', path: '/getting-started' },
        { label: 'Examples', path: '/examples' },
        { label: 'Spring', path: '/spring' },
        { label: 'FAQ', path: '/faq' },
    ];

    const drawer = (
        <Box sx={{ width: 250 }}>
            <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h6" component="div">
                    JCacheX
                </Typography>
                <IconButton onClick={handleDrawerToggle}>
                    <CloseIcon />
                </IconButton>
            </Box>
            <List>
                {navigationLinks.map((link) => (
                    <ListItem key={link.path} disablePadding>
                        <ListItemButton
                            component={Link}
                            to={link.path}
                            selected={isActiveLink(link.path)}
                            onClick={handleDrawerToggle}
                        >
                            <ListItemText primary={link.label} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
            <Box sx={{ p: 2, mt: 'auto' }}>
                <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<GitHubIcon />}
                    href="https://github.com/dhruv1110/JCacheX"
                    target="_blank"
                    rel="noopener noreferrer"
                    sx={{ mb: 1 }}
                >
                    GitHub
                </Button>
                <Button
                    fullWidth
                    variant="contained"
                    component={Link}
                    to="/getting-started"
                >
                    Get Started
                </Button>
            </Box>
        </Box>
    );

    return (
        <>
            <AppBar position="fixed" color="default" elevation={0}>
                <Toolbar>
                    {/* Logo */}
                    <Typography
                        variant="h6"
                        component={Link}
                        to="/"
                        sx={{
                            mr: 4,
                            fontWeight: 700,
                            color: 'primary.main',
                            textDecoration: 'none',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 1,
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

                    {/* Desktop Navigation */}
                    {!isMobile && (
                        <Box sx={{ display: 'flex', gap: 1, mr: 'auto' }}>
                            {navigationLinks.map((link) => (
                                <Button
                                    key={link.path}
                                    component={Link}
                                    to={link.path}
                                    color={isActiveLink(link.path) ? 'primary' : 'inherit'}
                                    variant={isActiveLink(link.path) ? 'contained' : 'text'}
                                    size="small"
                                >
                                    {link.label}
                                </Button>
                            ))}
                        </Box>
                    )}

                    {/* Desktop Actions */}
                    {!isMobile && (
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button
                                variant="outlined"
                                startIcon={<GitHubIcon />}
                                href="https://github.com/dhruv1110/JCacheX"
                                target="_blank"
                                rel="noopener noreferrer"
                                size="small"
                            >
                                GitHub
                            </Button>
                            <Button
                                variant="contained"
                                component={Link}
                                to="/getting-started"
                                size="small"
                            >
                                Get Started
                            </Button>
                        </Box>
                    )}

                    {/* Mobile Menu Button */}
                    {isMobile && (
                        <IconButton
                            color="inherit"
                            aria-label="open drawer"
                            edge="end"
                            onClick={handleDrawerToggle}
                            sx={{ ml: 'auto' }}
                        >
                            <MenuIcon />
                        </IconButton>
                    )}
                </Toolbar>
            </AppBar>

            {/* Mobile Drawer */}
            <Drawer
                variant="temporary"
                anchor="right"
                open={mobileOpen}
                onClose={handleDrawerToggle}
                ModalProps={{
                    keepMounted: true, // Better open performance on mobile.
                }}
            >
                {drawer}
            </Drawer>

            {/* Toolbar spacer */}
            <Toolbar />
        </>
    );
};

export default Navbar;
