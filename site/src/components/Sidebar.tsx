import React, { useState } from 'react';
import {
    Drawer,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Collapse,
    Typography,
    IconButton,
    Box,
    Divider,
    useTheme,
    useMediaQuery,
} from '@mui/material';
import {
    ExpandLess,
    ExpandMore,
    Close as CloseIcon,
} from '@mui/icons-material';

interface NavigationItem {
    id: string;
    title: string;
    icon: React.ReactNode;
    children?: NavigationItem[];
}

interface SidebarProps {
    open: boolean;
    onClose: () => void;
    navigationItems: NavigationItem[];
    title: string;
    expandedByDefault?: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({
    open,
    onClose,
    navigationItems,
    title,
    expandedByDefault = false
}) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    // Initialize expanded items based on expandedByDefault prop
    const [expandedItems, setExpandedItems] = useState<string[]>(() => {
        if (expandedByDefault && !isMobile) {
            return navigationItems.map(item => item.id);
        }
        return [];
    });

    const handleItemClick = (itemId: string) => {
        // Implement smooth scrolling to section
        const element = document.getElementById(itemId);
        if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
            if (isMobile) {
                onClose();
            }
        }
    };

    const toggleExpanded = (itemId: string) => {
        setExpandedItems(prev =>
            prev.includes(itemId)
                ? prev.filter(id => id !== itemId)
                : [...prev, itemId]
        );
    };

    const renderNavigationItem = (item: NavigationItem, level: number = 0) => (
        <React.Fragment key={item.id}>
            <ListItem disablePadding>
                <ListItemButton
                    onClick={() => {
                        if (item.children) {
                            toggleExpanded(item.id);
                        } else {
                            handleItemClick(item.id);
                        }
                    }}
                    sx={{
                        pl: { xs: level * 1.5 + 2, md: level * 2 + 2 },
                        py: { xs: 1.5, md: 0.5 },
                        minHeight: { xs: 48, md: 'auto' }, // Better touch targets on mobile
                        borderRadius: { xs: 2, md: 0 },
                        mx: { xs: 1, md: 0 },
                        mb: { xs: 0.5, md: 0 },
                        '&:hover': {
                            backgroundColor: theme.palette.action.hover,
                        },
                        '&:active': {
                            backgroundColor: { xs: theme.palette.action.selected, md: theme.palette.action.hover },
                        },
                    }}
                >
                    <ListItemIcon sx={{
                        minWidth: { xs: 40, md: 36 },
                        color: theme.palette.text.secondary,
                        '& svg': {
                            fontSize: { xs: '1.4rem', md: '1.25rem' }
                        }
                    }}>
                        {item.icon}
                    </ListItemIcon>
                    <ListItemText
                        primary={item.title}
                        primaryTypographyProps={{
                            variant: level === 0 ? (isMobile ? 'subtitle1' : 'subtitle1') : (isMobile ? 'body1' : 'body2'),
                            fontWeight: level === 0 ? 600 : (isMobile ? 500 : 400),
                            fontSize: level === 0 ? (isMobile ? '1rem' : '0.875rem') : (isMobile ? '0.9rem' : '0.8rem'),
                            lineHeight: 1.4,
                        }}
                    />
                    {item.children && (
                        expandedItems.includes(item.id) ?
                            <ExpandLess sx={{ fontSize: { xs: '1.5rem', md: '1.25rem' } }} /> :
                            <ExpandMore sx={{ fontSize: { xs: '1.5rem', md: '1.25rem' } }} />
                    )}
                </ListItemButton>
            </ListItem>
            {item.children && (
                <Collapse in={expandedItems.includes(item.id)} timeout="auto" unmountOnExit>
                    <List component="div" disablePadding>
                        {item.children.map(child => renderNavigationItem(child, level + 1))}
                    </List>
                </Collapse>
            )}
        </React.Fragment>
    );

    return (
        <Drawer
            anchor="left"
            open={open}
            onClose={onClose}
            variant={isMobile ? 'temporary' : 'persistent'}
            sx={{
                width: { xs: '85vw', sm: '70vw', md: 280 },
                maxWidth: { xs: 320, md: 280 },
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: { xs: '85vw', sm: '70vw', md: 280 },
                    maxWidth: { xs: 320, md: 280 },
                    boxSizing: 'border-box',
                    borderRight: `1px solid ${theme.palette.divider}`,
                    top: { xs: 0, md: 64 }, // Full height on mobile, account for navbar on desktop
                    height: { xs: '100vh', md: 'calc(100vh - 64px)' },
                    zIndex: isMobile ? 1400 : 1100,
                    position: 'fixed',
                    borderTopRightRadius: { xs: 16, md: 0 },
                    borderBottomRightRadius: { xs: 16, md: 0 },
                },
            }}
        >
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                p: { xs: 2.5, md: 2 },
                pt: { xs: 3, md: 2 }, // Extra top padding on mobile
                minHeight: { xs: 60, md: 'auto' }
            }}>
                <Typography
                    variant={isMobile ? "h5" : "h6"}
                    component="h2"
                    sx={{
                        fontWeight: 700,
                        color: theme.palette.primary.main,
                        fontSize: { xs: '1.25rem', md: '1.125rem' }
                    }}
                >
                    {title}
                </Typography>
                {isMobile && (
                    <IconButton
                        onClick={onClose}
                        size="medium"
                        sx={{
                            backgroundColor: 'action.hover',
                            '&:hover': {
                                backgroundColor: 'action.selected',
                            }
                        }}
                    >
                        <CloseIcon />
                    </IconButton>
                )}
            </Box>
            <Divider />
            <List sx={{
                pt: { xs: 2, md: 1 },
                pb: { xs: 2, md: 1 },
                px: { xs: 0, md: 0 }
            }}>
                {navigationItems.map(item => renderNavigationItem(item))}
            </List>
        </Drawer>
    );
};

export default Sidebar;
