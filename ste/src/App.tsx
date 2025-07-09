import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './components/Home';
import GettingStarted from './components/GettingStarted';
import Examples from './components/Examples';
import SpringGuide from './components/SpringGuide';
import FAQ from './components/FAQ';
import './styles/App.css';

const App: React.FC = () => {
    return (
        <div className="App">
            <Navbar />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/getting-started" element={<GettingStarted />} />
                <Route path="/examples/*" element={<Examples />} />
                <Route path="/spring" element={<SpringGuide />} />
                <Route path="/spring-boot" element={<SpringGuide />} />
                <Route path="/faq" element={<FAQ />} />
                <Route path="/docs/getting-started" element={<GettingStarted />} />
                <Route path="/docs/examples" element={<Examples />} />
                <Route path="/docs/spring" element={<SpringGuide />} />
                <Route path="*" element={<Home />} />
            </Routes>
        </div>
    );
};

export default App;
