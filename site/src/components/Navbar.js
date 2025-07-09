import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const location = useLocation();

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const closeMenu = () => {
        setIsMenuOpen(false);
    };

    const isActive = (path) => {
        return location.pathname === path;
    };

    return (
        <nav className="navbar">
            <div className="container">
                <div className="navbar-brand">
                    <Link to="/" className="brand-link" onClick={closeMenu}>
                        <img src="/logo.svg" alt="JCacheX" className="brand-logo" />
                        <span className="brand-text">JCacheX</span>
                    </Link>
                </div>

                <div className={`navbar-menu ${isMenuOpen ? 'active' : ''}`}>
                    <div className="navbar-nav">
                        <Link
                            to="/"
                            className={`nav-link ${isActive('/') ? 'active' : ''}`}
                            onClick={closeMenu}
                        >
                            Home
                        </Link>
                        <Link
                            to="/getting-started"
                            className={`nav-link ${isActive('/getting-started') ? 'active' : ''}`}
                            onClick={closeMenu}
                        >
                            Getting Started
                        </Link>
                        <Link
                            to="/examples"
                            className={`nav-link ${isActive('/examples') ? 'active' : ''}`}
                            onClick={closeMenu}
                        >
                            Examples
                        </Link>
                        <Link
                            to="/spring"
                            className={`nav-link ${isActive('/spring') || isActive('/spring-boot') ? 'active' : ''}`}
                            onClick={closeMenu}
                        >
                            Spring Boot
                        </Link>
                        <a
                            href="https://javadoc.io/doc/io.github.dhruv1110/jcachex-core"
                            className="nav-link"
                            target="_blank"
                            rel="noopener noreferrer"
                            onClick={closeMenu}
                        >
                            API Docs
                        </a>
                        <a
                            href="https://github.com/dhruv1110/JCacheX"
                            className="nav-link"
                            target="_blank"
                            rel="noopener noreferrer"
                            onClick={closeMenu}
                        >
                            GitHub
                        </a>
                    </div>
                </div>

                <button
                    className={`navbar-toggle ${isMenuOpen ? 'active' : ''}`}
                    onClick={toggleMenu}
                    aria-label="Toggle navigation"
                >
                    <span className="toggle-bar"></span>
                    <span className="toggle-bar"></span>
                    <span className="toggle-bar"></span>
                </button>
            </div>
        </nav>
    );
};

export default Navbar;
