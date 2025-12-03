import axios from 'axios';

const API_BASE_URL = 'http://localhost:8085/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Analysis APIs
export const analysisApi = {
    analyzeService: (serviceName) => api.post(`/analysis/${serviceName}`),
    analyzeAll: () => api.post('/analysis/all'),
    getLatestReport: (serviceName) => api.get(`/analysis/${serviceName}/latest`),
    getHistory: (serviceName) => api.get(`/analysis/${serviceName}/history`),
    getStatus: (serviceName) => api.get(`/analysis/status/${serviceName}`),
    getAllStatus: () => api.get('/analysis/status'),
};

// Breaking Changes APIs
export const breakingChangesApi = {
    getAll: (serviceName) => api.get(`/breaking-changes/${serviceName}`),
    getByType: (serviceName, type) => api.get(`/breaking-changes/${serviceName}/type/${type}`),
    getCount: (serviceName) => api.get(`/breaking-changes/${serviceName}/count`),
    getSummary: (serviceName) => api.get(`/breaking-changes/${serviceName}/summary`),
    getRecent: (serviceName, limit = 10) => api.get(`/breaking-changes/${serviceName}/recent?limit=${limit}`),
    getStatistics: () => api.get('/breaking-changes/statistics'),
};

// API Specs APIs
export const specsApi = {
    getLatest: (serviceName) => api.get(`/specs/${serviceName}/latest`),
    getHistory: (serviceName) => api.get(`/specs/${serviceName}/history`),
    getByVersion: (serviceName, version) => api.get(`/specs/${serviceName}/version/${version}`),
};

// Baseline APIs
export const baselineApi = {
    getBaseline: (serviceName) => api.get(`/baseline/${serviceName}`),
    setBaseline: (serviceName, specId) => api.post(`/baseline/${serviceName}/set/${specId}`),
    setLatestAsBaseline: (serviceName) => api.post(`/baseline/${serviceName}/set-latest`),
    clearBaseline: (serviceName) => api.delete(`/baseline/${serviceName}`),
};

// Status Management APIs
export const statusApi = {
    updateStatus: (changeId, statusData) =>
        api.put(`/breaking-changes/${changeId}/status`, statusData),
    acknowledge: (changeId, acknowledgedBy) =>
        api.post(`/breaking-changes/${changeId}/acknowledge`, { acknowledgedBy }),
    resolve: (changeId, resolvedBy, notes) =>
        api.post(`/breaking-changes/${changeId}/resolve`, { resolvedBy, notes }),
    ignore: (changeId, ignoredBy, reason) =>
        api.post(`/breaking-changes/${changeId}/ignore`, { ignoredBy, reason }),
    getByStatus: (serviceName, status) =>
        api.get(`/breaking-changes/${serviceName}/status/${status}`),
    getActive: (serviceName) =>
        api.get(`/breaking-changes/${serviceName}/active`),
};

export default api;