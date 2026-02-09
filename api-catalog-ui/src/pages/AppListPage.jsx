import React, { useEffect, useState } from 'react';
import axios from 'axios';
import AppCard from '../components/AppCard';
import CreateAppModal from '../components/CreateAppModal';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const AppListPage = () => {
  const [apps, setApps] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchApps = () => {
    axios.get(`${API_BASE}/applications`)
      .then(res => setApps(res.data.applications))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    fetchApps();
  }, []);

  return (
    <div className="animate-in fade-in duration-700">
      <div className="flex justify-between items-end mb-12">
        <div className="text-left">
          <h1 className="text-3xl font-black text-[#1a2b49] mb-2 uppercase tracking-tighter">Mes Applications</h1>
          <div className="w-20 h-1.5 bg-[#3ab1bb] rounded-full"></div>
        </div>
        
        {/* BOUTON AJOUTER APPLICATION */}
        <button 
          onClick={() => setIsModalOpen(true)}
          className="bg-[#1a2b49] text-white px-6 py-3 rounded-xl font-bold text-sm hover:bg-slate-800 transition-all flex items-center gap-2 shadow-lg"
        >
          <span className="text-xl">+</span> AJOUTER UNE APPLICATION
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {apps.map(app => (
          <AppCard key={app.applicationId} app={app} />
        ))}
      </div>

      {/* MODAL DE CRÉATION */}
      <CreateAppModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onAppCreated={fetchApps} 
      />
    </div>
  );
};

export default AppListPage;