import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './components/Home';
import GettingStarted from './components/GettingStarted';
import Examples from './components/Examples';
import './styles/App.css';

function App() {
    return (
        <div className="App">
            <Navbar />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/docs/getting-started" element={<GettingStarted />} />
                <Route path="/examples" element={<Examples />} />
            </Routes>
        </div>
    );
}

export default App;
