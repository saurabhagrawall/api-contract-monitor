import React, { useState } from 'react';
import { analysisApi, baselineApi } from '../services/api';
import { CheckCircle, XCircle, Loader, Play, Star, StarOff } from 'lucide-react';
import './ServiceStatus.css';

const ServiceStatus = ({ services, onAnalysisComplete }) => {
    const [analyzing, setAnalyzing] = useState({});
    const [analyzingAll, setAnalyzingAll] = useState(false);
    const [settingBaseline, setSettingBaseline] = useState({});
    const [baselines, setBaselines] = useState({});

    // Fetch baselines on component mount
    React.useEffect(() => {
        fetchBaselines();
    }, []);

    const fetchBaselines = async () => {
        const serviceNames = Object.keys(services);
        const baselinePromises = serviceNames.map(async (serviceName) => {
            try {
                const response = await baselineApi.getBaseline(serviceName);
                return { serviceName, hasBaseline: response.data.hasBaseline, baseline: response.data.baseline };
            } catch (error) {
                return { serviceName, hasBaseline: false };
            }
        });

        const results = await Promise.all(baselinePromises);
        const baselinesMap = {};
        results.forEach(result => {
            baselinesMap[result.serviceName] = result;
        });
        setBaselines(baselinesMap);
    };

    const analyzeService = async (serviceName) => {
        setAnalyzing(prev => ({ ...prev, [serviceName]: true }));

        try {
            const response = await analysisApi.analyzeService(serviceName);
            console.log(`Analysis complete for ${serviceName}:`, response.data);

            alert(`✅ ${serviceName} analysis complete!\n\n${response.data.message}`);

            if (onAnalysisComplete) {
                onAnalysisComplete();
            }

            // Refresh baselines after analysis
            fetchBaselines();
        } catch (error) {
            console.error(`Error analyzing ${serviceName}:`, error);

            if (error.response?.status === 404) {
                alert(`❌ ${serviceName} is not running.\n\nPlease start the service first.`);
            } else {
                alert(`❌ Error analyzing ${serviceName}:\n\n${error.message}`);
            }
        } finally {
            setAnalyzing(prev => ({ ...prev, [serviceName]: false }));
        }
    };

    const analyzeAllServices = async () => {
        setAnalyzingAll(true);

        try {
            const response = await analysisApi.analyzeAll();
            console.log('Analyze all complete:', response.data);

            alert(`✅ All services analyzed!\n\nCheck the results below.`);

            if (onAnalysisComplete) {
                onAnalysisComplete();
            }

            fetchBaselines();
        } catch (error) {
            console.error('Error analyzing all services:', error);
            alert(`❌ Error analyzing services:\n\n${error.message}`);
        } finally {
            setAnalyzingAll(false);
        }
    };

    const setAsBaseline = async (serviceName) => {
        setSettingBaseline(prev => ({ ...prev, [serviceName]: true }));

        try {
            const response = await baselineApi.setLatestAsBaseline(serviceName);
            console.log(`Baseline set for ${serviceName}:`, response.data);

            alert(`✅ Baseline set successfully!\n\n${serviceName} latest version is now the baseline.`);

            // Refresh baselines
            fetchBaselines();
        } catch (error) {
            console.error(`Error setting baseline for ${serviceName}:`, error);
            alert(`❌ Error setting baseline:\n\n${error.response?.data?.error || error.message}`);
        } finally {
            setSettingBaseline(prev => ({ ...prev, [serviceName]: false }));
        }
    };

    const clearBaseline = async (serviceName) => {
        if (!window.confirm(`Are you sure you want to clear the baseline for ${serviceName}?`)) {
            return;
        }

        try {
            await baselineApi.clearBaseline(serviceName);
            alert(`✅ Baseline cleared for ${serviceName}`);
            fetchBaselines();
        } catch (error) {
            console.error(`Error clearing baseline:`, error);
            alert(`❌ Error clearing baseline:\n\n${error.message}`);
        }
    };

    return (
        <div className="service-status-section">
            <div className="section-header">
                <h2>Services Status</h2>
                <button
                    className="analyze-all-btn"
                    onClick={analyzeAllServices}
                    disabled={analyzingAll}
                >
                    {analyzingAll ? (
                        <>
                            <Loader className="spin" size={18} />
                            Analyzing All...
                        </>
                    ) : (
                        <>
                            <Play size={18} />
                            Analyze All Services
                        </>
                    )}
                </button>
            </div>

            <div className="services-grid">
                {Object.entries(services).map(([serviceName, isOnline]) => {
                    const baselineInfo = baselines[serviceName] || {};
                    const hasBaseline = baselineInfo.hasBaseline;

                    return (
                        <div key={serviceName} className="service-card">
                            <div className="service-info">
                                <div className={`service-status-icon ${isOnline ? 'online' : 'offline'}`}>
                                    {isOnline ? (
                                        <CheckCircle size={24} />
                                    ) : (
                                        <XCircle size={24} />
                                    )}
                                </div>

                                <div className="service-details">
                                    <div className="service-header">
                                        <h3>{serviceName}</h3>
                                        {hasBaseline && (
                                            <span className="baseline-badge" title="Baseline is set">
                                                <Star size={14} />
                                                Baseline
                                            </span>
                                        )}
                                    </div>
                                    <span className={`status-badge ${isOnline ? 'online' : 'offline'}`}>
                                        {isOnline ? 'Online' : 'Offline'}
                                    </span>
                                    {hasBaseline && baselineInfo.baseline && (
                                        <div className="baseline-info">
                                            <small>
                                                Set: {new Date(baselineInfo.baseline.baselineSetAt).toLocaleDateString()}
                                            </small>
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="service-actions">
                                <button
                                    className="analyze-btn"
                                    onClick={() => analyzeService(serviceName)}
                                    disabled={analyzing[serviceName]}
                                >
                                    {analyzing[serviceName] ? (
                                        <>
                                            <Loader className="spin" size={16} />
                                            Analyzing...
                                        </>
                                    ) : (
                                        <>
                                            <Play size={16} />
                                            Analyze Now
                                        </>
                                    )}
                                </button>

                                {hasBaseline ? (
                                    <button
                                        className="baseline-btn clear"
                                        onClick={() => clearBaseline(serviceName)}
                                        title="Clear baseline"
                                    >
                                        <StarOff size={16} />
                                        Clear
                                    </button>
                                ) : (
                                    <button
                                        className="baseline-btn set"
                                        onClick={() => setAsBaseline(serviceName)}
                                        disabled={settingBaseline[serviceName]}
                                        title="Set latest as baseline"
                                    >
                                        {settingBaseline[serviceName] ? (
                                            <>
                                                <Loader className="spin" size={16} />
                                                Setting...
                                            </>
                                        ) : (
                                            <>
                                                <Star size={16} />
                                                Set Baseline
                                            </>
                                        )}
                                    </button>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default ServiceStatus;