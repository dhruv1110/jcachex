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
    Info as InfoIcon,
    Rocket as RocketIcon,
    Settings as SettingsIcon,
    Code as CodeIcon,
    Extension as ExtensionIcon,
    Speed as SpeedIcon,
    Coffee as JavaIcon,
    Android as AndroidIcon,
    Dashboard as DashboardIcon,
    Analytics as AnalyticsIcon,
    Storage as StorageIcon,
    Timer as TimerIcon,
    Memory as MemoryIcon,
    Security as SecurityIcon,
    Architecture as ArchitectureIcon,
    CloudSync as CloudSyncIcon,
    Api as ApiIcon,
    Build as BuildIcon,
} from '@mui/icons-material';

interface NavigationItem {
    id: string;
    title: string;
    icon: React.ReactNode;
    children?: NavigationItem[];
}

interface MobileSidebarProps {
    open: boolean;
    onClose: () => void;
}

const MobileSidebar: React.FC<MobileSidebarProps> = ({ open, onClose }) => {
    const [expandedItems, setExpandedItems] = useState<string[]>([]);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const navigationItems: NavigationItem[] = [
        {
            id: 'introduction',
            title: 'Introduction',
            icon: <InfoIcon />,
            children: [
                { id: 'what-is-jcachex', title: 'What is JCacheX?', icon: <InfoIcon /> },
                { id: 'supported-platforms', title: 'Supported Platforms', icon: <AndroidIcon /> },
                { id: 'quick-start', title: '1 Minute Quick Start', icon: <RocketIcon /> },
            ],
        },
        {
            id: 'advanced-configurations',
            title: 'Advanced Configurations',
            icon: <SettingsIcon />,
            children: [
                { id: 'eviction-strategies', title: 'Eviction Strategies', icon: <DashboardIcon /> },
                { id: 'all-configuration-options', title: 'All Configuration Options', icon: <ApiIcon /> },
                { id: 'kotlin-extensions', title: 'Kotlin Extensions', icon: <ExtensionIcon /> },
            ],
        },
        {
            id: 'java-configuration',
            title: 'Java Configuration',
            icon: <JavaIcon />,
            children: [
                { id: 'java-basic-usage', title: 'Basic Usage', icon: <CodeIcon /> },
                { id: 'java-advanced-features', title: 'Advanced Features', icon: <BuildIcon /> },
                { id: 'java-best-practices', title: 'Best Practices', icon: <SecurityIcon /> },
            ],
        },
        {
            id: 'kotlin-configuration',
            title: 'Kotlin Configuration',
            icon: <ExtensionIcon />,
            children: [
                { id: 'kotlin-dsl', title: 'DSL Configuration', icon: <CodeIcon /> },
                { id: 'kotlin-coroutines', title: 'Coroutine Support', icon: <CloudSyncIcon /> },
                { id: 'kotlin-operators', title: 'Operator Overloading', icon: <ApiIcon /> },
            ],
        },
        {
            id: 'spring-boot-configuration',
            title: 'Spring Boot Configuration',
            icon: <SpeedIcon />,
            children: [
                { id: 'spring-annotations', title: 'Annotations', icon: <CodeIcon /> },
                { id: 'spring-properties', title: 'Properties Configuration', icon: <SettingsIcon /> },
                { id: 'spring-auto-configuration', title: 'Auto Configuration', icon: <BuildIcon /> },
            ],
        },
    ];

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
                        pl: level * 2 + 2,
                        py: 1,
                        '&:hover': {
                            backgroundColor: theme.palette.action.hover,
                        },
                    }}
                >
                    <ListItemIcon sx={{ minWidth: 36 }}>
                        {item.icon}
                    </ListItemIcon>
                    <ListItemText
                        primary={item.title}
                        primaryTypographyProps={{
                            variant: level === 0 ? 'subtitle1' : 'body2',
                            fontWeight: level === 0 ? 600 : 400,
                        }}
                    />
                    {item.children && (
                        expandedItems.includes(item.id) ? <ExpandLess /> : <ExpandMore />
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
                width: 280,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: 280,
                    boxSizing: 'border-box',
                    borderRight: `1px solid ${theme.palette.divider}`,
                },
            }}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 2 }}>
                <Typography variant="h6" component="h2" sx={{ fontWeight: 700, color: theme.palette.primary.main }}>
                    Documentation
                </Typography>
                <IconButton onClick={onClose} size="small">
                    <CloseIcon />
                </IconButton>
            </Box>
            <Divider />
            <List sx={{ pt: 1 }}>
                {navigationItems.map(item => renderNavigationItem(item))}
            </List>
        </Drawer>
    );
};

export default MobileSidebar;
