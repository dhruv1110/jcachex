import React, { useState, useRef, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import type { NavigationItem } from '../types';
import { NAVIGATION_ITEMS, MOBILE_NAVIGATION_ITEMS } from '../constants/navigation';
import './Navbar.css';

interface DropdownMenuProps {
    items: NavigationItem[];
    isOpen: boolean;
    onClose: () => void;
}

const DropdownMenu: React.FC<DropdownMenuProps> = ({ items, isOpen, onClose }) => {
    if (!isOpen || !items.length) return null;

    return (
        <div className="dropdown-menu">
            <div className="dropdown-content">
                {items.map((item) => (
                    <div key={item.id} className="dropdown-item">
                        {item.path ? (
                            <Link
                                to={item.path}
                                className="dropdown-link"
                                onClick={onClose}
                            >
                                {item.icon && <span className="dropdown-icon">{item.icon}</span>}
                                <div className="dropdown-text">
                                    <span className="dropdown-label">{item.label}</span>
                                </div>
                            </Link>
                        ) : item.href ? (
                            <a
                                href={item.href}
                                className="dropdown-link"
                                target={item.target}
                                rel="noopener noreferrer"
                                onClick={onClose}
                            >
                                {item.icon && <span className="dropdown-icon">{item.icon}</span>}
                                <div className="dropdown-text">
                                    <span className="dropdown-label">{item.label}</span>
                                </div>
                            </a>
                        ) : null}
                    </div>
                ))}
            </div>
        </div>
    );
};

interface NavItemProps {
    item: NavigationItem;
    isActive: boolean;
    onMobileClick: () => void;
}

const NavItem: React.FC<NavItemProps> = ({ item, isActive, onMobileClick }) => {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setIsDropdownOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleMouseEnter = () => {
        if (item.children?.length) {
            setIsDropdownOpen(true);
        }
    };

    const handleMouseLeave = () => {
        setIsDropdownOpen(false);
    };

    const handleClick = () => {
        if (item.children?.length) {
            setIsDropdownOpen(!isDropdownOpen);
        } else {
            onMobileClick();
        }
    };

    const handleDropdownClose = () => {
        setIsDropdownOpen(false);
        onMobileClick();
    };

    if (item.children?.length) {
        return (
            <div
                ref={dropdownRef}
                className={`nav-item dropdown ${isDropdownOpen ? 'open' : ''}`}
                onMouseEnter={handleMouseEnter}
                onMouseLeave={handleMouseLeave}
            >
                <button
                    className={`nav-link dropdown-toggle ${isActive ? 'active' : ''}`}
                    onClick={handleClick}
                    aria-haspopup="true"
                    aria-expanded={isDropdownOpen}
                >
                    {item.icon && <span className="nav-icon">{item.icon}</span>}
                    <span className="nav-label">{item.label}</span>
                    <svg
                        className={`dropdown-arrow ${isDropdownOpen ? 'open' : ''}`}
                        width="12"
                        height="12"
                        viewBox="0 0 12 12"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            d="M3 4.5L6 7.5L9 4.5"
                            stroke="currentColor"
                            strokeWidth="1.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                    </svg>
                </button>
                <DropdownMenu
                    items={item.children}
                    isOpen={isDropdownOpen}
                    onClose={handleDropdownClose}
                />
            </div>
        );
    }

    return (
        <div className="nav-item">
            {item.path ? (
                <Link
                    to={item.path}
                    className={`nav-link ${isActive ? 'active' : ''}`}
                    onClick={onMobileClick}
                >
                    {item.icon && <span className="nav-icon">{item.icon}</span>}
                    <span className="nav-label">{item.label}</span>
                </Link>
            ) : item.href ? (
                <a
                    href={item.href}
                    className={`nav-link ${isActive ? 'active' : ''}`}
                    target={item.target}
                    rel="noopener noreferrer"
                    onClick={onMobileClick}
                >
                    {item.icon && <span className="nav-icon">{item.icon}</span>}
                    <span className="nav-label">{item.label}</span>
                </a>
            ) : null}
        </div>
    );
};

const Navbar: React.FC = () => {
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const location = useLocation();

    const toggleMobileMenu = () => {
        setIsMobileMenuOpen(!isMobileMenuOpen);
    };

    const closeMobileMenu = () => {
        setIsMobileMenuOpen(false);
    };

    const isPathActive = (item: NavigationItem): boolean => {
        if (item.path) {
            return location.pathname === item.path;
        }
        if (item.children) {
            return item.children.some(child => child.path && location.pathname.startsWith(child.path));
        }
        return false;
    };

    // Close mobile menu when clicking outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            const navbar = document.querySelector('.navbar-container');
            if (navbar && !navbar.contains(event.target as Node)) {
                closeMobileMenu();
            }
        };

        if (isMobileMenuOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isMobileMenuOpen]);

    // Close mobile menu on route change
    useEffect(() => {
        closeMobileMenu();
    }, [location.pathname]);

    return (
        <nav className="navbar">
            <div className="navbar-container">
                {/* Brand */}
                <div className="navbar-brand">
                    <Link to="/" className="brand-link" onClick={closeMobileMenu}>
                        <span className="brand-text">JCacheX</span>
                    </Link>
                </div>

                {/* Desktop Navigation */}
                <div className="navbar-nav desktop">
                    {NAVIGATION_ITEMS.map((item) => (
                        <NavItem
                            key={item.id}
                            item={item}
                            isActive={isPathActive(item)}
                            onMobileClick={closeMobileMenu}
                        />
                    ))}
                </div>

                {/* Mobile Menu Toggle */}
                <button
                    className={`mobile-menu-toggle ${isMobileMenuOpen ? 'open' : ''}`}
                    onClick={toggleMobileMenu}
                    aria-label="Toggle navigation menu"
                    aria-expanded={isMobileMenuOpen}
                >
                    <span className="toggle-line"></span>
                    <span className="toggle-line"></span>
                    <span className="toggle-line"></span>
                </button>

                {/* Mobile Navigation */}
                <div className={`navbar-nav mobile ${isMobileMenuOpen ? 'open' : ''}`}>
                    <div className="mobile-nav-content">
                        {MOBILE_NAVIGATION_ITEMS.map((item) => (
                            <div key={item.id} className="mobile-nav-item">
                                {item.path ? (
                                    <Link
                                        to={item.path}
                                        className={`mobile-nav-link ${isPathActive(item) ? 'active' : ''}`}
                                        onClick={closeMobileMenu}
                                    >
                                        {item.icon && <span className="mobile-nav-icon">{item.icon}</span>}
                                        <span className="mobile-nav-label">{item.label}</span>
                                    </Link>
                                ) : item.href ? (
                                    <a
                                        href={item.href}
                                        className="mobile-nav-link"
                                        target={item.target}
                                        rel="noopener noreferrer"
                                        onClick={closeMobileMenu}
                                    >
                                        {item.icon && <span className="mobile-nav-icon">{item.icon}</span>}
                                        <span className="mobile-nav-label">{item.label}</span>
                                    </a>
                                ) : null}
                            </div>
                        ))}
                    </div>
                </div>

                {/* Mobile Menu Backdrop */}
                {isMobileMenuOpen && (
                    <div className="mobile-menu-backdrop" onClick={closeMobileMenu} />
                )}
            </div>
        </nav>
    );
};

export default Navbar;
