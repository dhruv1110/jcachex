#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const testVersion = process.env.REACT_APP_VERSION || '1.0.0';
const buildDir = path.join(__dirname, 'build');

console.log(`🔍 Verifying version replacement for: ${testVersion}`);

// Check if build directory exists
if (!fs.existsSync(buildDir)) {
    console.error('❌ Build directory not found. Please run "npm run build:local" first.');
    process.exit(1);
}

// Find the main JS file
const staticJsDir = path.join(buildDir, 'static', 'js');
const jsFiles = fs.readdirSync(staticJsDir).filter(file => file.startsWith('main.') && file.endsWith('.js'));

if (jsFiles.length === 0) {
    console.error('❌ No main JS file found in build directory.');
    process.exit(1);
}

const mainJsFile = path.join(staticJsDir, jsFiles[0]);
const jsContent = fs.readFileSync(mainJsFile, 'utf8');

// Check for version occurrences
const versionMatches = jsContent.match(new RegExp(testVersion.replace(/\./g, '\\.'), 'g'));
const versionCount = versionMatches ? versionMatches.length : 0;

console.log(`📦 Main JS file: ${jsFiles[0]}`);
console.log(`🔢 Version "${testVersion}" found ${versionCount} times in JS bundle`);

// Check HTML file
const indexHtml = path.join(buildDir, 'index.html');
const htmlContent = fs.readFileSync(indexHtml, 'utf8');
const htmlVersionMatches = htmlContent.match(new RegExp(testVersion.replace(/\./g, '\\.'), 'g'));
const htmlVersionCount = htmlVersionMatches ? htmlVersionMatches.length : 0;

console.log(`📄 Version "${testVersion}" found ${htmlVersionCount} times in HTML`);

// Verify critical paths
const criticalPaths = [
    `implementation 'io.github.dhruv1110:jcachex-core:${testVersion}'`,
    `<version>${testVersion}</version>`,
    `jcachex-core" % "${testVersion}"`
];

let pathsFound = 0;
criticalPaths.forEach(path => {
    if (jsContent.includes(path)) {
        pathsFound++;
        console.log(`✅ Found: ${path}`);
    } else {
        console.log(`❌ Missing: ${path}`);
    }
});

console.log(`\n📊 Summary:`);
console.log(`   Total version occurrences: ${versionCount + htmlVersionCount}`);
console.log(`   Critical paths found: ${pathsFound}/${criticalPaths.length}`);

if (versionCount > 0 && pathsFound === criticalPaths.length) {
    console.log(`✅ Version replacement is working correctly!`);
    console.log(`🌐 You can test the site at: http://localhost:3001`);
} else {
    console.log(`❌ Version replacement may not be working correctly.`);
    process.exit(1);
}
