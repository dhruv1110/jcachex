import React from 'react';
import {
    Box,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Chip,
    Typography,
    Divider,
    Paper
} from '@mui/material';

// Reusable component interfaces
interface FeatureItem {
    icon: React.ReactElement;
    primary: string;
    secondary: string;
}

interface CardHeaderProps {
    icon: React.ReactElement;
    title: string;
    subtitle: string;
    bgColor: string;
}

interface FeatureSectionProps {
    leftTitle: string;
    leftIcon: React.ReactElement;
    leftItems: FeatureItem[];
    rightTitle: string;
    rightIcon: React.ReactElement;
    rightItems: FeatureItem[];
    leftBgColor?: string;
    rightBgColor?: string;
}

interface CardFooterProps {
    chips: Array<{
        icon: React.ReactElement;
        label: string;
        color: 'primary' | 'secondary' | 'success' | 'info' | 'warning';
    }>;
    performanceText?: string;
}

interface EnhancedCardProps {
    header: CardHeaderProps;
    description: string;
    features: FeatureSectionProps;
    footer: CardFooterProps;
    children?: React.ReactNode;
}

// Reusable Components
const CardHeader: React.FC<CardHeaderProps> = ({ icon, title, subtitle, bgColor }) => (
    <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Box sx={{
            p: 2,
            bgcolor: bgColor,
            color: 'white',
            borderRadius: 2,
            mr: 2,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
        }}>
            {React.cloneElement(icon as React.ReactElement, { sx: { fontSize: 28 } })}
        </Box>
        <Box>
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                {title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
                {subtitle}
            </Typography>
        </Box>
    </Box>
);

const FeatureSection: React.FC<FeatureSectionProps> = ({
    leftTitle, leftIcon, leftItems,
    rightTitle, rightIcon, rightItems,
    leftBgColor = 'grey.50',
    rightBgColor = 'primary.50'
}) => (
    <Box sx={{
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
        gap: 3
    }}>
        <Paper sx={{ p: 3, bgcolor: leftBgColor, borderRadius: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                {React.cloneElement(leftIcon as React.ReactElement, { sx: { mr: 1, color: 'success.main' } })}
                {leftTitle}
            </Typography>
            <List dense>
                {leftItems.map((item, index) => (
                    <ListItem key={index} sx={{ pl: 0 }}>
                        <ListItemIcon sx={{ minWidth: 32 }}>
                            {React.cloneElement(item.icon as React.ReactElement, { color: 'primary', fontSize: 'small' })}
                        </ListItemIcon>
                        <ListItemText
                            primary={item.primary}
                            secondary={item.secondary}
                        />
                    </ListItem>
                ))}
            </List>
        </Paper>

        <Paper sx={{ p: 3, bgcolor: rightBgColor, borderRadius: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, mb: 2, display: 'flex', alignItems: 'center' }}>
                {React.cloneElement(rightIcon as React.ReactElement, { sx: { mr: 1, color: 'success.main' } })}
                {rightTitle}
            </Typography>
            <List dense>
                {rightItems.map((item, index) => (
                    <ListItem key={index} sx={{ pl: 0 }}>
                        <ListItemIcon sx={{ minWidth: 32 }}>
                            {React.cloneElement(item.icon as React.ReactElement, { color: 'success', fontSize: 'small' })}
                        </ListItemIcon>
                        <ListItemText
                            primary={item.primary}
                            secondary={item.secondary}
                        />
                    </ListItem>
                ))}
            </List>
        </Paper>
    </Box>
);

const CardFooter: React.FC<CardFooterProps> = ({ chips, performanceText }) => (
    <Box sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: 2
    }}>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {chips.map((chip, index) => (
                <Chip
                    key={index}
                    icon={chip.icon}
                    label={chip.label}
                    color={chip.color}
                    size="small"
                />
            ))}
        </Box>
        {performanceText && (
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }}>
                {performanceText}
            </Typography>
        )}
    </Box>
);

const EnhancedCard: React.FC<EnhancedCardProps> = ({ header, description, features, footer, children }) => (
    <Card sx={{
        mb: 4,
        transition: 'all 0.3s ease-in-out',
        '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: (theme) => theme.shadows[12],
        }
    }}>
        <CardContent sx={{ p: 4 }}>
            <CardHeader {...header} />

            <Typography variant="body1" sx={{ mb: 3, lineHeight: 1.7 }}>
                {description}
            </Typography>

            <FeatureSection {...features} />

            <Divider sx={{ my: 3 }} />

            <CardFooter {...footer} />
        </CardContent>
    </Card>
);

export default EnhancedCard;
export type { CardHeaderProps, FeatureSectionProps, CardFooterProps, EnhancedCardProps, FeatureItem };

// Helper function for concise card info item creation
export function ExampleCardInfoItem(icon: React.ReactElement, primary: string, secondary: string): FeatureItem {
    return { icon, primary, secondary };
}
