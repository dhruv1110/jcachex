import React from 'react';
import { HelmetProvider } from 'react-helmet-async';
import Navbar from './Navbar';
import Footer from './Footer';
import './Layout.css';

interface LayoutProps {
    children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
    return (
        <HelmetProvider>
            <div className="layout">
                <Navbar />
                <main className="layout__main">
                    {children}
                </main>
                <Footer />
            </div>
        </HelmetProvider>
    );
};

export default Layout;
