import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

const Footer: React.FC = () => {
    const currentYear = new Date().getFullYear();

    return (
        <footer className="footer">
            <div className="footer__container">
                {/* Main Footer Content */}
                <div className="footer__main">
                    {/* Brand Section */}
                    <div className="footer__brand">
                        <Link to="/" className="footer__brand-link">
                            <svg className="footer__brand-logo" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                            </svg>
                            JCacheX
                        </Link>
                        <p className="footer__brand-description">
                            A high-performance, open source Java caching framework with distributed support,
                            intelligent eviction strategies, and seamless Spring Boot integration.
                        </p>
                        <div className="footer__social">
                            <a
                                href="https://github.com/dhruv1110/JCacheX"
                                className="footer__social-link"
                                target="_blank"
                                rel="noopener noreferrer"
                                aria-label="GitHub"
                            >
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                    <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z" />
                                </svg>
                            </a>
                        </div>
                    </div>

                    {/* Navigation Links */}
                    <div className="footer__links">
                        <div className="footer__links-section">
                            <h3 className="footer__links-title">Documentation</h3>
                            <ul className="footer__links-list">
                                <li><Link to="/getting-started" className="footer__link">Getting Started</Link></li>
                                <li><Link to="/examples" className="footer__link">Examples</Link></li>
                                <li><Link to="/spring" className="footer__link">Spring Guide</Link></li>
                                <li><Link to="/faq" className="footer__link">FAQ</Link></li>
                            </ul>
                        </div>

                        <div className="footer__links-section">
                            <h3 className="footer__links-title">Resources</h3>
                            <ul className="footer__links-list">
                                <li><a href="https://github.com/dhruv1110/JCacheX" className="footer__link" target="_blank" rel="noopener noreferrer">GitHub Repository</a></li>
                                <li><a href="https://github.com/dhruv1110/JCacheX/releases" className="footer__link" target="_blank" rel="noopener noreferrer">Releases</a></li>
                                <li><a href="https://github.com/dhruv1110/JCacheX/issues" className="footer__link" target="_blank" rel="noopener noreferrer">Report Issues</a></li>
                                <li><a href="https://github.com/dhruv1110/JCacheX/discussions" className="footer__link" target="_blank" rel="noopener noreferrer">Discussions</a></li>
                            </ul>
                        </div>

                        <div className="footer__links-section">
                            <h3 className="footer__links-title">Community</h3>
                            <ul className="footer__links-list">
                                <li><a href="https://github.com/dhruv1110/JCacheX/blob/main/CONTRIBUTING.md" className="footer__link" target="_blank" rel="noopener noreferrer">Contributing</a></li>
                                <li><a href="https://github.com/dhruv1110/JCacheX/blob/main/CODE_OF_CONDUCT.md" className="footer__link" target="_blank" rel="noopener noreferrer">Code of Conduct</a></li>
                                <li><a href="https://github.com/dhruv1110/JCacheX/blob/main/LICENSE" className="footer__link" target="_blank" rel="noopener noreferrer">License</a></li>
                            </ul>
                        </div>
                    </div>
                </div>

                {/* Footer Bottom */}
                <div className="footer__bottom">
                    <div className="footer__bottom-content">
                        <p className="footer__copyright">
                            © {currentYear} JCacheX. Released under the MIT License.
                        </p>
                        <p className="footer__credits">
                            Built with ❤️ for the Java community
                        </p>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
