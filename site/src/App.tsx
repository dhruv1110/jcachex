import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import Layout from './components/Layout';
import Home from './components/Home';
import GettingStarted from './components/GettingStarted';
import Examples from './components/Examples';
import SpringGuide from './components/SpringGuide';
import FAQPage from './components/FAQ';
import lightTheme from './theme';
import './styles/modern.scss';

const App: React.FC = () => {
    return (
        <ThemeProvider theme={lightTheme}>
            <CssBaseline />
            <Layout>
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/getting-started" element={<GettingStarted />} />
                    <Route path="/examples/*" element={<Examples />} />
                    <Route path="/spring" element={<SpringGuide />} />
                    <Route path="/spring-boot" element={<SpringGuide />} />
                    <Route path="/faq" element={<FAQPage />} />
                    <Route path="/docs/getting-started" element={<GettingStarted />} />
                    <Route path="/docs/examples" element={<Examples />} />
                    <Route path="/docs/spring" element={<SpringGuide />} />
                    <Route path="*" element={<Home />} />
                </Routes>
            </Layout>
        </ThemeProvider>
    );
};

export default App;
