import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

// Configure basename for routing
// In development: use "/" (localhost:3000/)
// In production: use "/jcachex" (dhruv1110.github.io/jcachex/)
const getBasename = () => {
    if (process.env.NODE_ENV === 'development') {
        return '/';
    }
    // Extract basename from homepage URL
    const homepage = process.env.PUBLIC_URL || '/jcachex';
    return new URL(homepage, 'https://example.com').pathname;
};

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <React.StrictMode>
        <BrowserRouter basename={getBasename()}>
            <App />
        </BrowserRouter>
    </React.StrictMode>
);
