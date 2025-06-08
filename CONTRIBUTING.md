# Contributing to JCacheX

Thank you for your interest in contributing to JCacheX! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/your-username/JCacheX.git`
3. Create a new branch: `git checkout -b feature/your-feature-name`

## Development Setup

1. Install JDK 8 or later
2. Install Gradle 7.0 or later
3. Run `./gradlew build` to verify your setup

## Project Structure

- `jcachex-core`: Core caching functionality
- `jcachex-spring`: Spring Boot integration
- `jcachex-testing`: Testing utilities
- `example`: Example projects

## Development Guidelines

### Code Style

- Follow the Kotlin style guide
- Use 4 spaces for indentation
- Keep lines under 120 characters
- Write meaningful commit messages

### Testing

- Write unit tests for all new features
- Maintain test coverage above 90%
- Run `./gradlew test` before submitting a PR

### Documentation

- Add KDoc comments for all public APIs
- Update README.md for significant changes
- Include usage examples in documentation

### Pull Requests

1. Update documentation
2. Add tests
3. Run all tests: `./gradlew test`
4. Run code style checks: `./gradlew ktlintCheck detekt`
5. Submit a PR with a clear description

## Release Process

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create a release tag
4. Deploy to Maven Central

## Questions?

Feel free to open an issue for any questions or concerns.
