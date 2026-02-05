import React from 'react';
import { useNavigate } from 'react-router-dom';

const AppCard = ({ app }) => {
  const navigate = useNavigate();

  return (
    <div 
      onClick={() => navigate(`/application/${app.applicationId}`)}
      className="bg-white rounded-[2rem] shadow-sm p-8 flex flex-col items-center text-center border border-gray-100 transition-all hover:shadow-2xl hover:-translate-y-2 cursor-pointer group"
    >
      {/* Icône Application (Style Bureau/App) */}
      <div className="w-16 h-16 bg-cyan-50 rounded-2xl flex items-center justify-center mb-6 group-hover:bg-[#3ab1bb]/10 transition-colors">
        <svg className="w-8 h-8 text-[#3ab1bb]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      </div>

      <div className="flex-grow">
        <h3 className="text-[#1a2b49] font-bold text-xl mb-2 uppercase tracking-tight">
          {app.name}
        </h3>
        <p className="text-gray-400 text-sm mb-4 italic">
          {app.description || "Aucune description fournie."}
        </p>
      </div>
      
      {/* Statistiques rapides */}
      <div className="flex gap-4 mb-6">
        <div className="text-center">
          <p className="text-[10px] text-gray-400 uppercase font-bold tracking-widest">Souscriptions</p>
          <p className="text-lg font-black text-[#1a2b49]">{app.subscriptionCount}</p>
        </div>
        <div className="w-px h-8 bg-gray-100"></div>
        <div className="text-center">
          <p className="text-[10px] text-gray-400 uppercase font-bold tracking-widest">Quota</p>
          <p className="text-sm font-bold text-[#3ab1bb]">{app.throttlingPolicy}</p>
        </div>
      </div>

      <div className="w-full py-3 border-2 border-[#3ab1bb] text-[#3ab1bb] rounded-xl font-bold text-xs uppercase tracking-widest group-hover:bg-[#3ab1bb] group-hover:text-white transition-all">
        Gérer l'application
      </div>
    </div>
  );
};

export default AppCard;