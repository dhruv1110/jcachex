// Navigation types
export interface NavigationItem {
    id: string;
    label: string;
    path?: string;
    href?: string;
    icon?: string;
    children?: NavigationItem[];
    target?: '_blank' | '_self';
}

// Code tab types
export interface CodeTab {
    id: string;
    label: string;
    language: string;
    code: string;
}

// Feature types
export interface Feature {
    icon: string;
    title: string;
    description: string;
    details?: string[];
    href?: string;
}

// Eviction strategy types
export interface EvictionStrategy {
    name: string;
    title: string;
    description: string;
    useCase: string;
    icon?: string;
}

// Module types
export interface Module {
    name: string;
    title: string;
    description: string;
    features: string[];
    icon?: string;
}

// Badge types
export type BadgeVariant =
    | 'default'
    | 'primary'
    | 'success'
    | 'warning'
    | 'error'
    | 'info'
    | 'github'
    | 'maven'
    | 'gradle'
    | 'kotlin'
    | 'java'
    | 'spring';

export type BadgeSize = 'small' | 'medium' | 'large';

// Button types
export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost';
export type ButtonSize = 'sm' | 'md' | 'lg';

// Section types
export type SectionBackground = 'default' | 'light' | 'dark' | 'gradient' | 'primary';
export type SectionPadding = 'none' | 'sm' | 'md' | 'lg' | 'xl';

// Grid types
export type GridColumns = 1 | 2 | 3 | 4 | 5 | 6;
export type GridGap = 'none' | 'sm' | 'md' | 'lg' | 'xl';

// Theme types
export interface Theme {
    colors: {
        primary: string;
        secondary: string;
        background: string;
        surface: string;
        text: string;
        textLight: string;
        border: string;
        success: string;
        warning: string;
        error: string;
    };
    spacing: {
        xs: string;
        sm: string;
        md: string;
        lg: string;
        xl: string;
        '2xl': string;
    };
    borderRadius: string;
    transitions: {
        fast: string;
        base: string;
        slow: string;
    };
}

// Installation guide types
export interface InstallationTab extends CodeTab {
    packageManager?: 'maven' | 'gradle' | 'sbt' | 'npm' | 'yarn';
}

// FAQ types
export interface FAQ {
    id: string;
    question: string;
    answer: string;
    category?: string;
}

// Component props
export interface BaseComponentProps {
    className?: string;
    children?: React.ReactNode;
}

// Hook types
export interface UseVersionReturn {
    version: string;
    replaceVersion: (text: string) => string;
    replaceVersionInTabs: (tabs: CodeTab[]) => CodeTab[];
}

export interface UseTabStateReturn {
    activeTab: string;
    setActiveTab: (tabId: string) => void;
    switchTab: (tabId: string) => void;
    initializeTab: (tabs: CodeTab[]) => void;
}

export interface UseResponsiveReturn {
    isMobile: boolean;
    isTablet: boolean;
    isDesktop: boolean;
    screenSize: 'mobile' | 'tablet' | 'desktop';
}

// Example types
export interface ExampleCategory {
    id: string;
    icon: string;
    title: string;
    description: string;
    tabs: CodeTab[];
}

// Statistics types
export interface PerformanceStat {
    label: string;
    value: string;
    description: string;
    icon?: string;
}

// Architecture component types
export interface ArchitectureComponent {
    name: string;
    description: string;
    icon?: string;
    details?: string[];
}

// Resource types
export interface Resource {
    title: string;
    description: string;
    icon: string;
    href: string;
    badge: BadgeVariant;
    target?: '_blank' | '_self';
}

// Spring annotation types
export interface SpringAnnotation {
    name: string;
    description: string;
    example: string;
    parameters?: {
        name: string;
        type: string;
        description: string;
        required?: boolean;
    }[];
}

// SEO types
export interface SEOData {
    title: string;
    description: string;
    keywords?: string[];
    canonical?: string;
    image?: string;
    type?: 'website' | 'article';
    author?: string;
    publishedTime?: string;
    modifiedTime?: string;
    section?: string;
    tags?: string[];
}

export interface BreadcrumbItem {
    label: string;
    path?: string;
    current?: boolean;
}

export interface StructuredData {
    '@context': string;
    '@type': string;
    [key: string]: any;
}

// Meta tag props
export interface MetaTagsProps {
    seo: SEOData;
    structuredData?: StructuredData | StructuredData[];
}

// SEO Hook types
export interface UseSEOReturn {
    updateSEO: (seoData: SEOData) => void;
    addStructuredData: (data: StructuredData) => void;
    removeStructuredData: (type: string) => void;
    getCurrentPageSEO: () => SEOData;
}
