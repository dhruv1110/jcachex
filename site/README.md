# JCacheX Website

This is the React-based website for JCacheX, built with Create React App.

## 🚀 Quick Start

### Prerequisites
- Node.js (version 18 or higher)
- npm (comes with Node.js)

### Local Development

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Start the development server:**
   ```bash
   npm start
   ```
   This will open the website at [http://localhost:3000](http://localhost:3000)

3. **Build for production:**
   ```bash
   npm run build
   ```
   This creates a `build` folder with the production-ready files.

4. **Test the production build locally:**
   ```bash
   npm install -g serve
   serve -s build
   ```

## 📁 Project Structure

```
site/
├── public/           # Static files
│   ├── index.html   # Main HTML template
│   └── logo.svg     # JCacheX logo
├── src/
│   ├── components/  # React components
│   │   ├── Home.js            # Homepage
│   │   ├── Navbar.js          # Navigation
│   │   ├── CodeTabs.js        # Code example tabs
│   │   ├── GettingStarted.js  # Documentation
│   │   └── Examples.js        # Code examples
│   ├── styles/      # CSS files
│   │   ├── index.css        # Global styles
│   │   └── App.css          # App-wide styles
│   ├── App.js       # Main App component
│   └── index.js     # React entry point
├── package.json     # Dependencies and scripts
└── README.md       # This file
```

## 🔧 Available Scripts

- `npm start` - Starts development server
- `npm run build` - Builds the app for production
- `npm test` - Runs tests
- `npm run eject` - Ejects from Create React App (⚠️ irreversible)

## 🎨 Features

- **Responsive Design**: Works on all devices
- **Code Highlighting**: Syntax highlighting for Java, Kotlin, Maven, Gradle
- **Tab Navigation**: Interactive code examples
- **Modern UI**: Clean, professional design
- **SEO Optimized**: Meta tags and structured data

## 🛠️ Development Tips

### Adding New Components
1. Create a new file in `src/components/`
2. Import and use in `App.js` or other components
3. Add corresponding CSS file if needed

### Updating Documentation
- Edit `src/components/GettingStarted.js` for documentation
- Edit `src/components/Examples.js` for code examples
- Version numbers are automatically updated during release

### Styling
- Use CSS variables defined in `src/styles/index.css`
- Follow the existing naming conventions
- Responsive design should be mobile-first

## 🚢 Deployment

The website is automatically deployed to GitHub Pages when:
1. A new release is created
2. Changes are pushed to the main branch

The deployment process:
1. Installs dependencies
2. Builds the React app
3. Deploys to GitHub Pages

## 🔗 URLs

- **Live Site**: https://dhruv1110.github.io/JCacheX
- **GitHub Repository**: https://github.com/dhruv1110/JCacheX
- **Maven Central**: https://central.sonatype.com/artifact/io.github.dhruv1110/jcachex-core

## 📝 Contributing

1. Make changes to the React components
2. Test locally with `npm start`
3. Build and test production version
4. Submit a pull request

## 🐛 Troubleshooting

### Development Server Won't Start
- Check Node.js version: `node --version`
- Delete `node_modules` and run `npm install` again
- Check for port conflicts (default port 3000)

### Build Failures
- Check for syntax errors in components
- Ensure all dependencies are installed
- Check console for specific error messages

### GitHub Pages Deployment Issues
- Verify the GitHub Pages workflow is enabled
- Check the Actions tab for deployment logs
- Ensure the `homepage` field in `package.json` is correct
