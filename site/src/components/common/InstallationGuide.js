import React from 'react';
import CodeTabs from '../CodeTabs';
import { useVersion } from '../../hooks';
import './InstallationGuide.css';

const InstallationGuide = ({
    tabs,
    title = "Installation",
    description = "Add JCacheX to your project using your preferred build tool:",
    className = ''
}) => {
    const { replaceVersionInTabs } = useVersion();
    const processedTabs = replaceVersionInTabs(tabs);

    return (
        <div className={`installation-guide ${className}`}>
            <div className="installation-header">
                <h2>{title}</h2>
                {description && <p>{description}</p>}
            </div>
            <div className="installation-content">
                <CodeTabs tabs={processedTabs} />
            </div>
        </div>
    );
};

export default InstallationGuide;
