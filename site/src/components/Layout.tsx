import React, { useState } from 'react';
import { Box, useTheme, useMediaQuery, IconButton } from '@mui/material';
import { Menu as MenuIcon } from '@mui/icons-material';
import Navbar from './Navbar';
import Footer from './Footer';
import Sidebar from './Sidebar';

interface NavigationItem {
    id: string;
    title: string;
    icon: React.ReactNode;
    children?: NavigationItem[];
}

interface SidebarConfig {
    title: string;
    navigationItems: NavigationItem[];
    expandedByDefault?: boolean;
}

interface LayoutProps {
    children: React.ReactNode;
    sidebarConfig?: SidebarConfig;
}

const Layout: React.FC<LayoutProps> = ({ children, sidebarConfig }) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    // Only show sidebar if config is provided with required data
    const showSidebar = Boolean(
        sidebarConfig &&
        sidebarConfig.title &&
        sidebarConfig.navigationItems &&
        sidebarConfig.navigationItems.length > 0
    );

    const [sidebarOpen, setSidebarOpen] = useState(!isMobile && showSidebar);

    const handleSidebarToggle = () => {
        setSidebarOpen(!sidebarOpen);
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                display: 'flex',
                flexDirection: 'column',
            }}
        >
            {/* Skip to content for accessibility */}
            <a href="#main-content" className="skip-link">Skip to content</a>
            <Navbar />
            <Box sx={{ display: 'flex', flex: 1, position: 'relative' }}>
                {showSidebar && sidebarConfig && (
                    <Sidebar
                        open={sidebarOpen}
                        onClose={() => setSidebarOpen(false)}
                        navigationItems={sidebarConfig.navigationItems}
                        title={sidebarConfig.title}
                        expandedByDefault={sidebarConfig.expandedByDefault}
                    />
                )}
                <Box
                    component="main"
                    id="main-content"
                    sx={{
                        flex: 1,
                        display: 'flex',
                        flexDirection: 'column',
                        marginLeft: showSidebar && !isMobile && sidebarOpen ? '32px' : 0,
                        minHeight: 'calc(100vh - 64px)', // Account for navbar
                        position: 'relative',
                        width: showSidebar && !isMobile && sidebarOpen ? 'calc(100% - 32px)' : '100%',
                        transition: theme.transitions.create(['margin', 'width'], {
                            easing: theme.transitions.easing.sharp,
                            duration: theme.transitions.duration.leavingScreen,
                        }),
                    }}
                >
                    {/* Mobile sidebar toggle button - only show if sidebar config exists */}
                    {showSidebar && isMobile && !sidebarOpen && (
                        <IconButton
                            color="primary"
                            aria-label="open sidebar"
                            onClick={handleSidebarToggle}
                            sx={{
                                position: 'fixed',
                                top: 88,
                                left: 20,
                                zIndex: 1250,
                                backgroundColor: theme.palette.primary.main,
                                color: 'white',
                                boxShadow: theme.shadows[8],
                                '&:hover': {
                                    backgroundColor: theme.palette.primary.dark,
                                    transform: 'scale(1.1)',
                                    boxShadow: theme.shadows[12],
                                },
                                '&:active': {
                                    transform: 'scale(0.95)',
                                },
                                width: 56,
                                height: 56,
                                border: `3px solid ${theme.palette.background.paper}`,
                                transition: theme.transitions.create(['transform', 'background-color', 'box-shadow'], {
                                    duration: theme.transitions.duration.short,
                                }),
                            }}
                        >
                            <MenuIcon sx={{ fontSize: 26 }} />
                        </IconButton>
                    )}
                    <Box sx={{ flex: 1 }}>
                        {children}
                    </Box>
                </Box>
            </Box>
            <Footer />
        </Box>
    );
};

export default Layout;
