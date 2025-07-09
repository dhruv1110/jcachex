import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
    const [isOpen, setIsOpen] = useState(false);
    const location = useLocation();

    const toggleMenu = () => {
        setIsOpen(!isOpen);
    };

    const isActive = (path) => {
        if (path === '/') {
            return location.pathname === '/';
        }
        return location.pathname.startsWith(path);
    };

    return (
        <header className="header">
            <nav className="navbar">
                <div className="container">
                    <div className="navbar-brand">
                        <Link to="/">
                            <img src="/logo.svg" alt="JCacheX Logo" className="logo" />
                            <span>JCacheX</span>
                        </Link>
                    </div>
                    <ul className={`navbar-nav ${isOpen ? 'active' : ''}`}>
                        <li><Link to="/" className={isActive('/') ? 'active' : ''}>Home</Link></li>
                        <li><a href="#features" className={isActive('/') ? 'active' : ''}>Features</a></li>
                        <li><a href="#quick-start" className={isActive('/') ? 'active' : ''}>Quick Start</a></li>
                        <li><Link to="/docs/getting-started" className={isActive('/docs') ? 'active' : ''}>Documentation</Link></li>
                        <li><Link to="/examples" className={isActive('/examples') ? 'active' : ''}>Examples</Link></li>
                        <li><a href="https://javadoc.io/doc/io.github.dhruv1110/jcachex-core/latest/index.html" target="_blank" rel="noopener noreferrer">API Reference</a></li>
                        <li><a href="https://github.com/dhruv1110/JCacheX" target="_blank" rel="noopener noreferrer">GitHub</a></li>
                    </ul>
                    <button
                        className={`mobile-menu-toggle ${isOpen ? 'active' : ''}`}
                        onClick={toggleMenu}
                        aria-label="Toggle mobile menu"
                    >
                        <span></span>
                        <span></span>
                        <span></span>
                    </button>
                </div>
            </nav>
        </header>
    );
};

export default Navbar;
