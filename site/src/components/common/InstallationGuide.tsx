import React from 'react';
import type { CodeTab } from '../../types';
import CodeTabs from '../CodeTabs';
import { useVersion } from '../../hooks';

interface InstallationGuideProps {
    tabs: CodeTab[];
    title?: string;
    description?: string;
    className?: string;
}

const InstallationGuide: React.FC<InstallationGuideProps> = ({
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
