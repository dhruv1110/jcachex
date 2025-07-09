import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import { HelmetProvider } from 'react-helmet-async';
import Layout from './components/Layout';
import Home from './components/Home';
import GettingStarted from './components/GettingStarted';
import ExamplesPage from './components/Examples';
import SpringGuide from './components/SpringGuide';
import FAQPage from './components/FAQ';
import DocumentationPage from './components/DocumentationPage';
import NotFoundPage from './components/NotFound';
import lightTheme from './theme';
import './styles/modern.scss';

const App: React.FC = () => {
    return (
        <HelmetProvider>
            <ThemeProvider theme={lightTheme}>
                <CssBaseline />
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/getting-started" element={<GettingStarted />} />
                    <Route path="/examples/*" element={<ExamplesPage />} />
                    <Route path="/spring" element={<SpringGuide />} />
                    <Route path="/spring-boot" element={<SpringGuide />} />
                    <Route path="/faq" element={<FAQPage />} />
                    <Route path="/docs" element={<DocumentationPage />} />
                    <Route path="/documentation" element={<DocumentationPage />} />
                    <Route path="/docs/getting-started" element={<GettingStarted />} />
                    <Route path="/docs/examples" element={<ExamplesPage />} />
                    <Route path="/docs/spring" element={<SpringGuide />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </ThemeProvider>
        </HelmetProvider>
    );
};

export default App;
