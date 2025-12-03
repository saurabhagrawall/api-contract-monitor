import React, { useState, useEffect } from 'react';
import { breakingChangesApi, analysisApi, statusApi } from '../services/api';
import { AlertTriangle, CheckCircle, Clock, TrendingUp } from 'lucide-react';
import ServiceStatus from '../components/ServiceStatus';
import './Overview.css';

const Overview = () => {
    const [stats, setStats] = useState({
        totalBreakingChanges: 0,
        servicesOnline: 0,
        totalServices: 4,
        recentChanges: [],
        servicesStatus: {},
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [expandedInsights, setExpandedInsights] = useState({});

    const services = ['user-service', 'order-service', 'product-service', 'notification-service'];

    useEffect(() => {
        fetchOverviewData();
    }, []);

    const fetchOverviewData = async () => {
        try {
            setLoading(true);

            const statsResponse = await breakingChangesApi.getStatistics();
            const statusResponse = await analysisApi.getAllStatus();

            const recentChangesPromises = services.map(service =>
                breakingChangesApi.getRecent(service, 5).catch(() => ({ data: [] }))
            );
            const recentChangesResults = await Promise.all(recentChangesPromises);
            const allRecentChanges = recentChangesResults
                .flatMap(result => result.data)
                .sort((a, b) => new Date(b.detectedAt) - new Date(a.detectedAt))
                .slice(0, 10);

            setStats({
                totalBreakingChanges: statsResponse.data.totalBreakingChanges || 0,
                servicesOnline: statusResponse.data.onlineCount || 0,
                totalServices: statusResponse.data.totalCount || 4,
                recentChanges: allRecentChanges,
                servicesStatus: statusResponse.data.services || {},
            });

            setLoading(false);
        } catch (err) {
            console.error('Error fetching overview data:', err);
            setError('Failed to load dashboard data. Make sure the backend is running on port 8085.');
            setLoading(false);
        }
    };

    const handleAcknowledge = async (changeId, serviceName) => {
        if (!window.confirm('Mark this breaking change as acknowledged?')) {
            return;
        }

        try {
            await statusApi.acknowledge(changeId, 'saurabh@umass.edu');
            alert('✅ Breaking change acknowledged!');
            fetchOverviewData();
        } catch (error) {
            console.error('Error acknowledging:', error);
            alert('❌ Error: ' + error.message);
        }
    };

    const handleResolve = async (changeId, serviceName) => {
        const notes = window.prompt('Enter resolution notes (optional):');
        if (notes === null) return;

        try {
            await statusApi.resolve(changeId, 'saurabh@umass.edu', notes || 'Resolved');
            alert('✅ Breaking change marked as resolved!');
            fetchOverviewData();
        } catch (error) {
            console.error('Error resolving:', error);
            alert('❌ Error: ' + error.message);
        }
    };

    const handleIgnore = async (changeId, serviceName) => {
        const reason = window.prompt('Why ignore this change?');
        if (reason === null) return;

        try {
            await statusApi.ignore(changeId, 'saurabh@umass.edu', reason || 'Marked as intentional');
            alert('✅ Breaking change marked as ignored!');
            fetchOverviewData();
        } catch (error) {
            console.error('Error ignoring:', error);
            alert('❌ Error: ' + error.message);
        }
    };

    const toggleInsights = (changeId) => {
        setExpandedInsights(prev => ({
            ...prev,
            [changeId]: !prev[changeId]
        }));
    };

    const filteredChanges = React.useMemo(() => {
        if (statusFilter === 'ALL') {
            return stats.recentChanges;
        }
        return stats.recentChanges.filter(change => change.status === statusFilter);
    }, [stats.recentChanges, statusFilter]);

    if (loading) {
        return (
            <div className="overview">
                <h1>Overview</h1>
                <div className="loading">Loading dashboard data...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="overview">
                <h1>Overview</h1>
                <div className="error-message">
                    <AlertTriangle size={24} />
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    return (
        <div className="overview">
            <div className="header">
                <h1>Dashboard Overview</h1>
                <p className="subtitle">Real-time monitoring of API contract changes</p>
            </div>

            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon breaking">
                        <AlertTriangle size={24} />
                    </div>
                    <div className="stat-content">
                        <p className="stat-label">Total Breaking Changes</p>
                        <h2 className="stat-value">{stats.totalBreakingChanges}</h2>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon online">
                        <CheckCircle size={24} />
                    </div>
                    <div className="stat-content">
                        <p className="stat-label">Services Online</p>
                        <h2 className="stat-value">{stats.servicesOnline}/{stats.totalServices}</h2>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon recent">
                        <Clock size={24} />
                    </div>
                    <div className="stat-content">
                        <p className="stat-label">Recent Changes</p>
                        <h2 className="stat-value">{stats.recentChanges.length}</h2>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon trend">
                        <TrendingUp size={24} />
                    </div>
                    <div className="stat-content">
                        <p className="stat-label">Active Services</p>
                        <h2 className="stat-value">{stats.totalServices}</h2>
                    </div>
                </div>
            </div>

            <ServiceStatus
                services={stats.servicesStatus}
                onAnalysisComplete={fetchOverviewData}
            />

            <div className="section">
                <div className="section-header-with-filter">
                    <h2>Recent Breaking Changes</h2>
                    <div className="filter-dropdown">
                        <label htmlFor="status-filter">Filter:</label>
                        <select
                            id="status-filter"
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="status-filter-select"
                        >
                            <option value="ALL">All Statuses</option>
                            <option value="ACTIVE">Active Only</option>
                            <option value="ACKNOWLEDGED">Acknowledged</option>
                            <option value="RESOLVED">Resolved</option>
                            <option value="IGNORED">Ignored</option>
                        </select>
                    </div>
                </div>
                {filteredChanges.length === 0 ? (
                    <div className="empty-state">
                        <CheckCircle size={48} />
                        <p>No breaking changes detected yet</p>
                        <p className="empty-state-subtitle">Your APIs are stable!</p>
                    </div>
                ) : (
                    <div className="changes-list">
                        {filteredChanges.map((change) => (
                            <div key={change.id} className="change-card">
                                <div className="change-header">
                                    <span className={`change-type ${change.changeType.toLowerCase()}`}>
                                        {change.changeType.replace('_', ' ')}
                                    </span>
                                    <span className={`status-badge status-${change.status?.toLowerCase() || 'active'}`}>
                                        {change.status || 'ACTIVE'}
                                    </span>
                                    <span className="change-service">{change.serviceName}</span>
                                    <span className="change-time">
                                        {new Date(change.detectedAt).toLocaleDateString()} at{' '}
                                        {new Date(change.detectedAt).toLocaleTimeString()}
                                    </span>
                                </div>
                                <div className="change-details">
                                    <p className="change-description">{change.description}</p>
                                    <p className="change-path">Path: {change.path}</p>
                                </div>
                                {change.aiSuggestion && (
                                    <div className="ai-insights-section">
                                        <button
                                            className="ai-insights-toggle"
                                            onClick={() => toggleInsights(change.id)}
                                        >
                                            <span className="ai-badge-inline">
                                                ✨ AI Insights Available
                                            </span>
                                            <span className="toggle-icon">
                                                {expandedInsights[change.id] ? '▼' : '▶'}
                                            </span>
                                        </button>

                                        {expandedInsights[change.id] && (
                                            <div className="ai-insights-content">
                                                {/* Migration Suggestion */}
                                                {change.aiSuggestion && (
                                                    <div className="insight-block">
                                                        <h4>🔧 Migration Strategy</h4>
                                                        <div className="insight-text">
                                                            {change.aiSuggestion.split('\n').map((line, i) => {
                                                                const trimmed = line.trim();
                                                                if (!trimmed) return null;

                                                                // Main numbered headers (1. **Text**)
                                                                const mainHeaderMatch = trimmed.match(/^(\d+)\.\s*\*\*(.*?)\*\*$/);
                                                                if (mainHeaderMatch) {
                                                                    return <p key={i} className="insight-main-header">{mainHeaderMatch[1]}. {mainHeaderMatch[2]}</p>;
                                                                }

                                                                // Section headers (### text or ⚠️ text)
                                                                if (trimmed.startsWith('###') || trimmed.startsWith('⚠️') || trimmed.startsWith('💡')) {
                                                                    return <p key={i} className="insight-section-header">{trimmed.replace(/^###\s*/, '').replace(/\*\*/g, '')}</p>;
                                                                }

                                                                // Sub-bullets (- - **Label:** text)
                                                                const subBulletMatch = trimmed.match(/^-\s*-\s*\*\*(.*?)\*\*\s*(.*)$/);
                                                                if (subBulletMatch) {
                                                                    return (
                                                                        <p key={i} className="insight-sub-bullet">
                                                                            <span className="insight-label">{subBulletMatch[1]}</span> {subBulletMatch[2]}
                                                                        </p>
                                                                    );
                                                                }

                                                                // Regular bullets (- text)
                                                                if (trimmed.startsWith('-')) {
                                                                    return <p key={i} className="insight-bullet">{trimmed.substring(1).trim().replace(/\*\*/g, '')}</p>;
                                                                }

                                                                // Regular text (remove any remaining **)
                                                                return <p key={i} className="insight-regular">{trimmed.replace(/\*\*/g, '')}</p>;
                                                            })}
                                                        </div>
                                                    </div>
                                                )}

                                                {/* Impact Prediction */}
                                                {change.predictedImpact && (
                                                    <div className="insight-block">
                                                        <h4>⚠️ Cross-Service Impact</h4>
                                                        <div className="insight-text">
                                                            {change.predictedImpact.split('\n').map((line, i) => {
                                                                const trimmed = line.trim();
                                                                if (!trimmed) return null;

                                                                // Service impact lines (1. service | 80% | description)
                                                                const impactMatch = trimmed.match(/^(\d+)\.\s*([^|]+)\s*\|\s*(\d+%)\s*\|\s*(.+)$/);
                                                                if (impactMatch) {
                                                                    return (
                                                                        <p key={i} className="impact-service">
                                                                            <span className="impact-number">{impactMatch[1]}.</span>
                                                                            <span className="impact-name">{impactMatch[2].trim()}</span>
                                                                            <span className="impact-confidence">{impactMatch[3]}</span>
                                                                            <span className="impact-description">{impactMatch[4]}</span>
                                                                        </p>
                                                                    );
                                                                }

                                                                // Section headers
                                                                if (trimmed.startsWith('###') || trimmed.startsWith('⚠️') || trimmed.startsWith('💡')) {
                                                                    return <p key={i} className="insight-section-header">{trimmed.replace(/^###\s*/, '').replace(/\*\*/g, '')}</p>;
                                                                }

                                                                // Regular text
                                                                return <p key={i} className="insight-regular">{trimmed.replace(/\*\*/g, '')}</p>;
                                                            })}
                                                        </div>
                                                    </div>
                                                )}

                                                {/* Plain English Explanation */}
                                                {change.plainEnglishExplanation && (
                                                    <div className="insight-block">
                                                        <h4>💡 Business Impact</h4>
                                                        <div className="insight-text">
                                                            {change.plainEnglishExplanation.split('\n').map((line, i) => {
                                                                const trimmed = line.trim();
                                                                if (!trimmed) return null;

                                                                // Numbered points (1. text)
                                                                const numberedMatch = trimmed.match(/^(\d+)\.\s*(.+)$/);
                                                                if (numberedMatch) {
                                                                    return <p key={i} className="business-point">{numberedMatch[1]}. {numberedMatch[2].replace(/\*\*/g, '')}</p>;
                                                                }

                                                                // Section headers
                                                                if (trimmed.startsWith('###') || trimmed.startsWith('⚠️') || trimmed.startsWith('💡')) {
                                                                    return <p key={i} className="insight-section-header">{trimmed.replace(/^###\s*/, '').replace(/\*\*/g, '')}</p>;
                                                                }

                                                                // Regular text
                                                                return <p key={i} className="insight-regular">{trimmed.replace(/\*\*/g, '')}</p>;
                                                            })}
                                                        </div>
                                                    </div>
                                                )}
                                            </div>
                                        )}
                                    </div>
                                )}

                                {change.status === 'ACTIVE' && (
                                    <div className="change-actions">
                                        <button
                                            className="action-btn acknowledge"
                                            onClick={() => handleAcknowledge(change.id, change.serviceName)}
                                        >
                                            Acknowledge
                                        </button>
                                        <button
                                            className="action-btn resolve"
                                            onClick={() => handleResolve(change.id, change.serviceName)}
                                        >
                                            Resolve
                                        </button>
                                        <button
                                            className="action-btn ignore"
                                            onClick={() => handleIgnore(change.id, change.serviceName)}
                                        >
                                            Ignore
                                        </button>
                                    </div>
                                )}

                                {change.status === 'RESOLVED' && change.resolvedAt && (
                                    <div className="resolution-info">
                                        <p>✅ Resolved by {change.resolvedBy} on {new Date(change.resolvedAt).toLocaleDateString()}</p>
                                        {change.resolutionNotes && <p className="notes">{change.resolutionNotes}</p>}
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Overview;