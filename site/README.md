# JCacheX Website

This is the React-based documentation website for JCacheX, built with Create React App.

## Project Structure

```
site/
├── public/                 # Static assets
├── src/
│   ├── components/        # React components
│   │   ├── common/       # Reusable components
│   │   ├── Home.js       # Home page component
│   │   ├── GettingStarted.js
│   │   ├── Examples.js
│   │   ├── SpringGuide.js
│   │   └── ...
│   ├── constants/        # Shared constants and data
│   ├── hooks/           # Custom React hooks
│   ├── styles/          # Global styles
│   └── ...
├── package.json
└── README.md
```

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn

### Installation

```bash
cd site
npm install
```

### Development

```bash
npm start
```

This runs the app in development mode. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### Building

```bash
npm run build
```

Builds the app for production to the `build` folder.

## Version Testing

The website supports dynamic version replacement for documentation and examples. Here's how to test different versions:

### Environment Variables

The website uses the `REACT_APP_VERSION` environment variable to display version information throughout the site.

### Testing with Different Versions

#### 1. Default Version (Development)
```bash
npm start
```
This will use the default version (`1.0.0`) defined in the constants file.

#### 2. Custom Version (Development)
```bash
REACT_APP_VERSION=2.1.0 npm start
```

#### 3. Custom Version (Build - Local Testing)
```bash
REACT_APP_VERSION=2.1.0 npm run build:local
npm run serve
```

#### 4. Custom Version (Build - GitHub Pages)
```bash
REACT_APP_VERSION=2.1.0 npm run build
```

#### 5. Testing Multiple Versions

**Important**: All npm commands must be run from the `site/` directory:

```bash
# Navigate to the site directory first
cd site

# Test with current version (local)
REACT_APP_VERSION=1.0.15 npm run build:local

# Test with beta version (local)
REACT_APP_VERSION=2.0.0-beta.1 npm run build:local

# Test with snapshot version (local)
REACT_APP_VERSION=1.1.0-SNAPSHOT npm run build:local

# Quick test command (builds and serves automatically)
REACT_APP_VERSION=2.1.0 npm run test:version
```

### Verification

After building with a custom version, check that:

1. **Installation tabs** display the correct version in Maven, Gradle, and SBT examples
2. **Code examples** use the correct version in import statements
3. **Links** point to the correct version in Maven Central/GitHub
4. **Documentation** reflects the correct version

#### Automated Verification
```bash
# Navigate to site directory
cd site

# Use the verification script to check version replacement
REACT_APP_VERSION=2.1.0 npm run verify:version

# Manual verification - check version in bundle
REACT_APP_VERSION=2.1.0 npm run build:local
grep -o "2\.1\.0" build/static/js/main.*.js
```

### Local Testing vs GitHub Pages

- **Local Testing**: Use `npm run build:local` - builds with relative paths for local testing
- **GitHub Pages**: Use `npm run build` - builds with `/jcachex/` homepage for GitHub Pages deployment

The local testing setup resolves Chrome console errors when serving GitHub Pages builds locally.

### Areas Where Version is Used

- Installation guides (Maven, Gradle, SBT)
- Code examples and snippets
- Download links
- API documentation links
- GitHub release links

## Development Guidelines

### Code Structure

#### Reusable Components
- Use components from `src/components/common/` for consistent UI
- Components include: `FeatureCard`, `Section`, `Grid`, `InstallationGuide`, `Badge`

#### Custom Hooks
- `useVersion`: Manages version replacement throughout the site
- `useTabState`: Handles tab switching logic
- `useResponsive`: Provides responsive design utilities

#### Constants
- All shared data is centralized in `src/constants/index.js`
- Includes installation tabs, code examples, and configuration

### Adding New Content

#### 1. New Component
```jsx
import React from 'react';
import { Section, Grid, FeatureCard } from './common';
import { useVersion } from '../hooks';

const NewComponent = () => {
    const { version } = useVersion();

    return (
        <Section title="New Section" centered>
            <Grid columns={3}>
                {/* content */}
            </Grid>
        </Section>
    );
};
```

#### 2. New Code Example
```jsx
const newTabs = [
    {
        id: 'java',
        label: 'Java',
        language: 'java',
        code: `// Example with version ${version}`
    }
];
```

#### 3. New Installation Guide
```jsx
<InstallationGuide
    tabs={customTabs}
    title="Custom Installation"
    description="Install with custom options"
/>
```

### Responsive Design

The website uses a mobile-first approach with breakpoints:

- Mobile: `< 768px`
- Tablet: `768px - 1024px`
- Desktop: `> 1024px`

#### CSS Guidelines
- Use CSS custom properties for theming
- Follow BEM naming convention
- Ensure all components are responsive
- Test on multiple screen sizes

### Performance

- Components use React hooks for state management
- Code splitting is handled by Create React App
- Images are optimized and use proper formats
- CSS is minified in production builds

## Deployment

### GitHub Pages

The site is configured for GitHub Pages deployment with the homepage set to `/jcachex/`.

```bash
# Navigate to site directory
cd site

# Build for production
npm run build

# Deploy to GitHub Pages (if gh-pages package is configured)
npm run deploy
```

**Note**: GitHub Pages deployment is typically handled automatically via GitHub Actions when you push to the main branch.

### Manual Deployment

1. Build the project:
```bash
npm run build
```

2. Upload the `build/` folder contents to your web server

3. Configure your server to serve `index.html` for all routes (SPA routing)

## Testing

### Visual Testing
```bash
npm start
```

Navigate through all pages and verify:
- All components render correctly
- Responsive design works on different screen sizes
- Version information is displayed correctly
- Code examples are properly formatted
- Links work correctly

### Build Testing
```bash
npm run build
```

Verify the build completes without errors and check the output for:
- Proper file sizes
- No console errors
- Correct version replacement

## Troubleshooting

### Common Issues

1. **Version not updating**: Clear browser cache and rebuild
2. **Components not rendering**: Check console for import errors
3. **Styles not applying**: Ensure CSS imports are correct
4. **Build failures**: Check for syntax errors and missing dependencies

### Debug Mode

```bash
# Enable debug logging
DEBUG=* npm start

# Check bundle analysis
npm run build && npx serve -s build
```

## Contributing

1. Follow the existing code structure
2. Test your changes with multiple versions
3. Ensure responsive design works
4. Update documentation as needed
5. Test the build process

## License

This project is licensed under the same license as JCacheX.
