# CI/CD Setup Guide for JCacheX

This document provides step-by-step instructions for setting up the complete CI/CD pipeline for JCacheX, including GitHub Actions, SonarQube, Codecov, and Maven Central publishing.

## Overview

The CI/CD pipeline includes:
- **GitHub Actions** for automated testing and building
- **SonarCloud** for code quality analysis
- **Codecov** for code coverage tracking
- **Maven Central** for artifact publishing
- **Automated versioning** and release management

## Required Accounts and Setup

### 1. SonarCloud Setup

1. Go to [SonarCloud](https://sonarcloud.io)
2. Sign in with your GitHub account
3. Create a new organization or use an existing one
4. Import your JCacheX project
5. Note your organization key and project key

### 2. Codecov Setup

1. Go to [Codecov](https://codecov.io)
2. Sign in with your GitHub account
3. Add your JCacheX repository
4. Get your upload token from the repository settings

### 3. Maven Central Setup

1. Create a Sonatype JIRA account at [issues.sonatype.org](https://issues.sonatype.org)
2. Create a new project ticket for `io.github.dpflux` groupId
3. Wait for approval (usually takes 1-2 business days)
4. Generate GPG keys for artifact signing:

```bash
# Generate GPG key
gpg --gen-key

# Export private key
gpg --export-secret-keys --armor YOUR_KEY_ID > private.key

# Get key ID
gpg --list-secret-keys --keyid-format LONG
```

## GitHub Secrets Configuration

Add the following secrets to your GitHub repository (Settings → Secrets and variables → Actions):

### SonarCloud Secrets
- `SONAR_TOKEN`: Your SonarCloud token

### Codecov Secrets
- `CODECOV_TOKEN`: Your Codecov upload token

### Maven Central Publishing Secrets
- `OSSRH_USERNAME`: Your Sonatype JIRA username
- `OSSRH_PASSWORD`: Your Sonatype JIRA password
- `GPG_PRIVATE_KEY`: Your GPG private key (base64 encoded)
- `GPG_PASSWORD`: Your GPG key passphrase

### Generate Base64 Encoded GPG Key

```bash
# Export and encode your GPG private key
gpg --export-secret-keys --armor YOUR_KEY_ID | base64 | tr -d '\n'
```

## Local Development Setup

1. Copy `gradle.properties.template` to `gradle.properties`
2. Fill in your credentials (never commit this file!)
3. Ensure you have JDK 17+ installed

```bash
# Copy template
cp gradle.properties.template gradle.properties

# Edit with your credentials
nano gradle.properties
```

## Workflow Overview

### PR Workflow (CI)
- Triggered on pull requests to `main` or `develop`
- Runs tests with coverage
- Performs code quality checks (Detekt, KtLint)
- Uploads coverage to Codecov
- Runs SonarQube analysis
- **Does not publish artifacts**

### Release Workflow
- Manually triggered via GitHub Actions UI
- Choose version increment type (patch/minor/major)
- Automatically calculates new version
- Runs full test suite
- Publishes to Maven Central
- Creates Git tag and GitHub release
- Updates to next SNAPSHOT version

## Release Process

1. Ensure all PRs are merged and tests pass
2. Go to Actions → Release workflow
3. Click "Run workflow"
4. Choose version increment type:
   - `patch`: 1.0.0 → 1.0.1 (bug fixes)
   - `minor`: 1.0.0 → 1.1.0 (new features)
   - `major`: 1.0.0 → 2.0.0 (breaking changes)
5. Add release notes (optional)
6. Click "Run workflow"

The workflow will:
1. Calculate new version
2. Update `build.gradle.kts`
3. Run tests
4. Publish to Maven Central
5. Create Git tag
6. Create GitHub release
7. Update to next SNAPSHOT version

## Version Management

Versions follow semantic versioning (SemVer):
- `MAJOR.MINOR.PATCH`
- Development versions use `-SNAPSHOT` suffix
- Release versions have no suffix

Current version is managed in `build.gradle.kts`:
```kotlin
version = "0.1.0-SNAPSHOT"
```

## Artifact Usage

After successful release, artifacts are available on Maven Central:

### Gradle
```kotlin
dependencies {
    implementation("io.github.dpflux:jcachex-core:x.y.z")
    implementation("io.github.dpflux:jcachex-kotlin:x.y.z")
    implementation("io.github.dpflux:jcachex-spring:x.y.z")
}
```

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-core</artifactId>
        <version>x.y.z</version>
    </dependency>
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-kotlin</artifactId>
        <version>x.y.z</version>
    </dependency>
    <dependency>
        <groupId>io.github.dpflux</groupId>
        <artifactId>jcachex-spring</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```

## Quality Gates

### Test Coverage
- Minimum 60% code coverage required (adjustable)
- Coverage reports uploaded to Codecov
- PR comments show coverage changes

### Code Quality
- SonarQube quality gate must pass
- Detekt and KtLint checks must pass
- No critical or blocker issues allowed

### Documentation Coverage
- Documentation coverage tracked for all modules
- Reports generated in CI/CD pipeline
- Undocumented files identified automatically

### Security
- Dependabot keeps dependencies updated
- GPG signing ensures artifact integrity
- Secrets are properly managed

## Documentation Features

### 📚 **Automatic Documentation Publishing**
- **GitHub Pages**: https://dpflux.github.io/JCacheX/
- **javadoc.io**: https://javadoc.io/doc/io.github.dpflux/

### 📊 **Documentation Coverage**
Track documentation coverage for your codebase:

```bash
# Check documentation coverage for all modules
./gradlew allDocumentationCoverage

# Check documentation coverage for specific module
./gradlew :jcachex-core:documentationCoverage
```

### 🔧 **Available Documentation Tasks**
- `./gradlew generateAllDocs` - Generate all documentation
- `./gradlew allDocumentationCoverage` - Check doc coverage
- `./gradlew javadoc` - Generate Javadoc for Java files
- `./gradlew dokkaHtml` - Generate KDoc for Kotlin files

## Monitoring and Badges

Add these badges to your README.md:

```markdown
[![CI](https://github.com/dpflux/JCacheX/workflows/CI/badge.svg)](https://github.com/dpflux/JCacheX/actions)
[![codecov](https://codecov.io/gh/dpflux/JCacheX/branch/main/graph/badge.svg)](https://codecov.io/gh/dpflux/JCacheX)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dpflux_JCacheX&metric=alert_status)](https://sonarcloud.io/dashboard?id=dpflux_JCacheX)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dpflux/jcachex-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.dpflux/jcachex-core)
[![Documentation](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://dpflux.github.io/JCacheX/)
[![javadoc](https://javadoc.io/badge2/io.github.dpflux/jcachex-core/javadoc.svg)](https://javadoc.io/doc/io.github.dpflux/jcachex-core)
```

## Troubleshooting

### Common Issues

1. **GPG Signing Fails**
   - Ensure GPG key is properly base64 encoded
   - Check passphrase is correct
   - Verify key hasn't expired

2. **Maven Central Upload Fails**
   - Check OSSRH credentials
   - Ensure project is approved by Sonatype
   - Verify all required POM fields are present

3. **SonarQube Analysis Fails**
   - Check SONAR_TOKEN is valid
   - Ensure project exists in SonarCloud
   - Verify organization key is correct

4. **Coverage Upload Fails**
   - Check CODECOV_TOKEN is valid
   - Ensure coverage files are generated
   - Verify file paths in workflow

### Getting Help

- Check GitHub Actions logs for detailed error messages
- Review SonarCloud project dashboard
- Check Codecov project settings
- Contact maintainers via GitHub issues

## Next Steps

1. Set up all required accounts and secrets
2. Test the CI workflow with a sample PR
3. Perform your first release
4. Monitor quality metrics and coverage
5. Add project badges to README
6. Document release process for contributors
