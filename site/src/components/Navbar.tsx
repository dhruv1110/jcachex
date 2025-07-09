import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';

// Icons
const MenuIcon = () => (
    <svg className="navbar__toggle-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
    </svg>
);

const CloseIcon = () => (
    <svg className="navbar__toggle-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
    </svg>
);

const ExternalIcon = () => (
    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
    </svg>
);

const Navbar: React.FC = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const location = useLocation();

    // Close mobile menu when route changes
    useEffect(() => {
        setIsMobileMenuOpen(false);
    }, [location]);

    // Close mobile menu on escape key
    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                setIsMobileMenuOpen(false);
            }
        };

        if (isMobileMenuOpen) {
            document.addEventListener('keydown', handleEscape);
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }

        return () => {
            document.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = '';
        };
    }, [isMobileMenuOpen]);

    const toggleMobileMenu = () => {
        setIsMobileMenuOpen(!isMobileMenuOpen);
    };

    const isActiveLink = (path: string) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    const navigationLinks = [
        { label: 'Docs', path: '/getting-started' },
        { label: 'Examples', path: '/examples' },
        { label: 'Spring', path: '/spring' },
        { label: 'FAQ', path: '/faq' },
    ];

    return (
        <>
            <nav className="navbar">
                <div className="navbar__container">
                    {/* Brand */}
                    <Link to="/" className="navbar__brand">
                        <svg className="navbar__brand-logo" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                        </svg>
                        JCacheX
                    </Link>

                    {/* Desktop Navigation */}
                    <div className="navbar__nav">
                        {navigationLinks.map((link) => (
                            <Link
                                key={link.path}
                                to={link.path}
                                className={`navbar__link ${isActiveLink(link.path) ? 'navbar__link--active' : ''}`}
                            >
                                {link.label}
                            </Link>
                        ))}
                    </div>

                    {/* Desktop Actions */}
                    <div className="navbar__actions">
                        <a
                            href="https://github.com/dhruv1110/JCacheX"
                            className="btn btn--ghost btn--sm"
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            <ExternalIcon />
                            GitHub
                        </a>
                        <Link to="/getting-started" className="btn btn--primary btn--sm">
                            Get Started
                        </Link>
                    </div>

                    {/* Mobile Menu Toggle */}
                    <button
                        className="navbar__toggle"
                        onClick={toggleMobileMenu}
                        aria-label="Toggle mobile menu"
                        aria-expanded={isMobileMenuOpen}
                    >
                        {isMobileMenuOpen ? <CloseIcon /> : <MenuIcon />}
                    </button>
                </div>

                {/* Mobile Menu */}
                <div className={`navbar__mobile ${isMobileMenuOpen ? 'navbar__mobile--open' : ''}`}>
                    <div className="navbar__mobile-nav">
                        {navigationLinks.map((link) => (
                            <Link
                                key={link.path}
                                to={link.path}
                                className={`navbar__mobile-link ${isActiveLink(link.path) ? 'navbar__mobile-link--active' : ''}`}
                            >
                                {link.label}
                            </Link>
                        ))}

                        <div className="navbar__mobile-actions">
                            <a
                                href="https://github.com/dhruv1110/JCacheX"
                                className="btn btn--secondary btn--block"
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                <ExternalIcon />
                                View on GitHub
                            </a>
                            <Link to="/getting-started" className="btn btn--primary btn--block">
                                Get Started
                            </Link>
                        </div>
                    </div>
                </div>
            </nav>

            {/* Mobile Menu Overlay */}
            <div
                className={`navbar-overlay ${isMobileMenuOpen ? 'navbar-overlay--open' : ''}`}
                onClick={() => setIsMobileMenuOpen(false)}
                aria-hidden="true"
            />
        </>
    );
};

export default Navbar;
