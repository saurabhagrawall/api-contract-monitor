import React, { useState } from 'react';
import { analysisApi } from '../services/api';
import { CheckCircle, XCircle, Loader, Play } from 'lucide-react';
import './ServiceStatus.css';

const ServiceStatus = ({ services, onAnalysisComplete }) => {
  const [analyzing, setAnalyzing] = useState({});
  const [analyzingAll, setAnalyzingAll] = useState(false);

  const analyzeService = async (serviceName) => {
    setAnalyzing(prev => ({ ...prev, [serviceName]: true }));
    
    try {
      const response = await analysisApi.analyzeService(serviceName);
      console.log(`Analysis complete for ${serviceName}:`, response.data);
      
      // Show success notification
      alert(`✅ ${serviceName} analysis complete!\n\n${response.data.message}`);
      
      // Refresh parent component
      if (onAnalysisComplete) {
        onAnalysisComplete();
      }
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
    } catch (error) {
      console.error('Error analyzing all services:', error);
      alert(`❌ Error analyzing services:\n\n${error.message}`);
    } finally {
      setAnalyzingAll(false);
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
        {Object.entries(services).map(([serviceName, isOnline]) => (
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
                <h3>{serviceName}</h3>
                <span className={`status-badge ${isOnline ? 'online' : 'offline'}`}>
                  {isOnline ? 'Online' : 'Offline'}
                </span>
              </div>
            </div>

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
          </div>
        ))}
      </div>
    </div>
  );
};

export default ServiceStatus;