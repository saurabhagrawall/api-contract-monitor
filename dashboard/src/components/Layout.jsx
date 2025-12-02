import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { Activity, AlertTriangle, BarChart3, Home } from 'lucide-react';
import './Layout.css';

const Layout = () => {
  return (
    <div className="layout">
      {/* Sidebar Navigation */}
      <aside className="sidebar">
        <div className="logo">
          <Activity size={32} />
          <h1>API Contract Monitor</h1>
        </div>
        
        <nav className="nav">
          <NavLink to="/" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            <Home size={20} />
            <span>Overview</span>
          </NavLink>
          
          <NavLink to="/breaking-changes" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            <AlertTriangle size={20} />
            <span>Breaking Changes</span>
          </NavLink>
          
          <NavLink to="/analytics" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            <BarChart3 size={20} />
            <span>Analytics</span>
          </NavLink>
        </nav>
        
        <div className="sidebar-footer">
          <p>© 2025 Saurabh Agrawal</p>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;