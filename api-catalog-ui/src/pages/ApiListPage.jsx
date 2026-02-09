import React, { useEffect, useState } from 'react';
import axios from 'axios';
// Base API URL (configurable via Vite env var VITE_API_BASE_URL)
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
import ApiCard from '../components/ApiCard';
import CreateApiModal from '../components/CreateApiModal';

const ApiListPage = () => {
  const [apis, setApis] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchApis = () => {
    axios.get(`${API_BASE}/apis`)
      .then(res => setApis(res.data.apis))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    fetchApis();
  }, []);

  return (
    <div className="animate-in fade-in duration-700">
      <div className="flex justify-between items-end mb-12">
        <div className="text-left">
          <h1 className="text-3xl font-black text-[#1a2b49] mb-2 uppercase tracking-tighter">Gestion du Catalogue</h1>
          <div className="w-20 h-1.5 bg-[#3ab1bb] rounded-full"></div>
        </div>
        
        {/* BOUTON CREER API */}
        <button 
          onClick={() => setIsModalOpen(true)}
          className="bg-[#1a2b49] text-white px-6 py-3 rounded-xl font-bold text-sm hover:bg-slate-800 transition-all flex items-center gap-2"
        >
          <span className="text-xl font-light">+</span> CRÉER ET PUBLIER UNE API
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
        {apis.map(api => (
          <ApiCard key={api.id} api={api} />
        ))}
      </div>

      {/* MODAL / SLIDE-OVER */}
      <CreateApiModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        onApiCreated={fetchApis} 
      />
    </div>
  );
};

export default ApiListPage;