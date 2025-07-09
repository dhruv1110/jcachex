import { createTheme, ThemeOptions } from '@mui/material/styles';

// Define color palette
const palette = {
    primary: {
        main: '#3b82f6', // Blue
        light: '#60a5fa',
        dark: '#1d4ed8',
        contrastText: '#ffffff',
    },
    secondary: {
        main: '#6366f1', // Indigo
        light: '#818cf8',
        dark: '#4338ca',
        contrastText: '#ffffff',
    },
    success: {
        main: '#10b981',
        light: '#34d399',
        dark: '#059669',
    },
    warning: {
        main: '#f59e0b',
        light: '#fbbf24',
        dark: '#d97706',
    },
    error: {
        main: '#ef4444',
        light: '#f87171',
        dark: '#dc2626',
    },
    info: {
        main: '#3b82f6',
        light: '#60a5fa',
        dark: '#1d4ed8',
    },
    grey: {
        50: '#f9fafb',
        100: '#f3f4f6',
        200: '#e5e7eb',
        300: '#d1d5db',
        400: '#9ca3af',
        500: '#6b7280',
        600: '#4b5563',
        700: '#374151',
        800: '#1f2937',
        900: '#111827',
    },
    background: {
        default: '#ffffff',
        paper: '#ffffff',
    },
    text: {
        primary: '#111827',
        secondary: '#6b7280',
    },
};

// Define typography
const typography = {
    fontFamily: [
        '-apple-system',
        'BlinkMacSystemFont',
        '"Segoe UI"',
        'Roboto',
        '"Helvetica Neue"',
        'Arial',
        'sans-serif',
    ].join(','),
    h1: {
        fontSize: '3.75rem',
        fontWeight: 800,
        lineHeight: 1.2,
        letterSpacing: '-0.025em',
    },
    h2: {
        fontSize: '3rem',
        fontWeight: 700,
        lineHeight: 1.25,
        letterSpacing: '-0.025em',
    },
    h3: {
        fontSize: '2.25rem',
        fontWeight: 600,
        lineHeight: 1.3,
    },
    h4: {
        fontSize: '1.875rem',
        fontWeight: 600,
        lineHeight: 1.4,
    },
    h5: {
        fontSize: '1.5rem',
        fontWeight: 600,
        lineHeight: 1.4,
    },
    h6: {
        fontSize: '1.25rem',
        fontWeight: 600,
        lineHeight: 1.4,
    },
    body1: {
        fontSize: '1rem',
        lineHeight: 1.75,
    },
    body2: {
        fontSize: '0.875rem',
        lineHeight: 1.6,
    },
};

// Define component overrides
const components = {
    MuiButton: {
        styleOverrides: {
            root: {
                textTransform: 'none' as const,
                borderRadius: 8,
                fontWeight: 600,
                padding: '10px 24px',
            },
            containedPrimary: {
                background: 'linear-gradient(135deg, #3b82f6 0%, #6366f1 100%)',
                '&:hover': {
                    background: 'linear-gradient(135deg, #1d4ed8 0%, #4338ca 100%)',
                },
            },
        },
    },
    MuiCard: {
        styleOverrides: {
            root: {
                borderRadius: 16,
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
                border: '1px solid #e5e7eb',
            },
        },
    },
    MuiAppBar: {
        styleOverrides: {
            root: {
                backgroundColor: 'rgba(255, 255, 255, 0.8)',
                backdropFilter: 'blur(20px)',
                borderBottom: '1px solid rgba(0, 0, 0, 0.1)',
                boxShadow: 'none',
            },
        },
    },
    MuiContainer: {
        styleOverrides: {
            root: {
                maxWidth: '1200px !important',
            },
        },
    },
};

// Dark theme variant
const darkPalette = {
    ...palette,
    mode: 'dark' as const,
    background: {
        default: '#0f172a',
        paper: '#1e293b',
    },
    text: {
        primary: '#f1f5f9',
        secondary: '#94a3b8',
    },
    grey: {
        ...palette.grey,
        50: '#1e293b',
        100: '#334155',
        200: '#475569',
        300: '#64748b',
        400: '#94a3b8',
        500: '#cbd5e1',
        600: '#e2e8f0',
        700: '#f1f5f9',
        800: '#f8fafc',
        900: '#ffffff',
    },
};

// Create light theme
export const lightTheme = createTheme({
    palette,
    typography,
    components,
    spacing: 8,
    shape: {
        borderRadius: 8,
    },
} as ThemeOptions);

// Create dark theme
export const darkTheme = createTheme({
    palette: darkPalette,
    typography,
    components: {
        ...components,
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: 'rgba(15, 23, 42, 0.8)',
                    backdropFilter: 'blur(20px)',
                    borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                    boxShadow: 'none',
                },
            },
        },
    },
    spacing: 8,
    shape: {
        borderRadius: 8,
    },
} as ThemeOptions);

// Default export (light theme)
export default lightTheme;
