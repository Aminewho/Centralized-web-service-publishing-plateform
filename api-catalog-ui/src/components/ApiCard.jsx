import React from 'react';
import { useNavigate } from 'react-router-dom';

const ApiCard = ({ api }) => {
  const navigate = useNavigate();

  // Fonction pour rediriger vers la page de détails
  const handleCardClick = () => {
    navigate(`/api/${api.id}`);
  };

  return (
    <div 
      onClick={handleCardClick}
      className="bg-white rounded-[2rem] shadow-sm p-10 flex flex-col items-center text-center border border-gray-100 transition-all hover:shadow-2xl hover:-translate-y-2 cursor-pointer group"
    >
      {/* Icône */}
      <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-[#3ab1bb]/10 transition-colors">
        <svg className="w-8 h-8 text-[#1a2b49]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
      </div>

      <div className="flex-grow">
        <h3 className="text-[#1a2b49] font-bold text-xl mb-3 uppercase tracking-tight">
          {api.name}
        </h3>
        <p className="text-gray-500 text-sm mb-6 line-clamp-2">
          {api.description || "Description du service non renseignée"}
        </p>
      </div>
      
      <div className="mb-6 text-[10px] font-bold text-[#3ab1bb] tracking-widest uppercase">
        Version {api.version} • {api.lifeCycleStatus}
      </div>

      {/* Bouton stylisé RNE */}
      <div className="w-full bg-[#3ab1bb] text-white py-3 rounded-xl font-bold text-sm flex items-center justify-center group-hover:bg-[#2d8d96] transition-colors shadow-lg shadow-cyan-100">
        Accès au service / الولوج إلى الخدمة <span className="ml-2">→</span>
      </div>
    </div>
  );
};

export default ApiCard;