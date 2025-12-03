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

    const services = ['user-service', 'order-service', 'product-service', 'notification-service'];

    useEffect(() => {
        fetchOverviewData();
    }, []);

    const fetchOverviewData = async () => {
        try {
            setLoading(true);

            // Fetch statistics
            const statsResponse = await breakingChangesApi.getStatistics();

            // Fetch all services status
            const statusResponse = await analysisApi.getAllStatus();

            // Fetch recent changes from all services
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

    // NEW: Status update handlers
    const handleAcknowledge = async (changeId, serviceName) => {
        if (!window.confirm('Mark this breaking change as acknowledged?')) {
            return;
        }

        try {
            await statusApi.acknowledge(changeId, 'saurabh@umass.edu');
            alert('✅ Breaking change acknowledged!');
            fetchOverviewData(); // Refresh the list
        } catch (error) {
            console.error('Error acknowledging:', error);
            alert('❌ Error: ' + error.message);
        }
    };

    const handleResolve = async (changeId, serviceName) => {
        const notes = window.prompt('Enter resolution notes (optional):');
        if (notes === null) return; // User clicked cancel

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
        if (reason === null) return; // User clicked cancel

        try {
            await statusApi.ignore(changeId, 'saurabh@umass.edu', reason || 'Marked as intentional');
            alert('✅ Breaking change marked as ignored!');
            fetchOverviewData();
        } catch (error) {
            console.error('Error ignoring:', error);
            alert('❌ Error: ' + error.message);
        }
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

            {/* Stats Cards */}
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

            {/* Service Status Section - NEW! */}
            <ServiceStatus
                services={stats.servicesStatus}
                onAnalysisComplete={fetchOverviewData}
            />

            {/* Recent Changes Feed */}
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
                                    <div className="ai-badge">
                                        <span>✨ AI Insights Available</span>
                                    </div>
                                )}

                                {/* NEW: Action buttons */}
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

                                {/* Show resolution info if resolved */}
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