import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import PolicySelector from '../components/PolicySelector';
// Base API URL (configurable via Vite env var VITE_API_BASE_URL)
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const ApiDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState(null);

  useEffect(() => {
    axios.get(`${API_BASE}/api/v1/publisher/apis/${id}/full-details`)
      .then(res => setDetails(res.data))
      .catch(err => console.error(err));
  }, [id]);

  if (!details) return <div className="text-center py-20">Chargement des paramètres WSO2...</div>;

  return (
    <div className="animate-in slide-in-from-bottom-4 duration-500">
      <button onClick={() => navigate(-1)} className="text-[#3ab1bb] font-bold mb-6 flex items-center hover:underline">
        ← Retour au catalogue
      </button>

      <div className="bg-white rounded-2xl shadow-sm border p-10">
        <div className="flex justify-between items-start border-b pb-8 mb-8">
          <div>
            <h1 className="text-4xl font-black text-[#1a2b49] uppercase leading-none">{details.name}</h1>
            <p className="text-[#3ab1bb] font-bold mt-2">ID: {details.id}</p>
          </div>
          <span className="bg-green-100 text-green-700 px-4 py-2 rounded-full font-bold text-sm">
            {details.lifeCycleStatus}
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
          {/* Section Technique */}
          <div className="space-y-6">
            <h2 className="text-lg font-bold text-[#1a2b49] border-l-4 border-[#3ab1bb] pl-4">Configurations Endpoints</h2>
            <div className="bg-gray-50 p-4 rounded-lg font-mono text-sm break-all">
              <p className="font-bold text-gray-400 mb-2">PRODUCTION URL:</p>
              {details.endpointConfig?.production_endpoints?.url}
            </div>
            

<div className="grid grid-cols-1 md:grid-cols-2 gap-12">
  <div className="space-y-6">
    {/* Ta section Endpoint existante ... */}
    
    {/* REMPLACE l'ancien affichage des politiques par le composant intelligent */}
    <h2 className="text-lg font-bold text-[#1a2b49] border-l-4 border-[#3ab1bb] pl-4 mb-4">
      Gestion des Politiques 
    </h2>
    <PolicySelector apiId={id} />
  </div>

  {/* Ta section Abonnements existante ... */}
</div>
          </div>

          {/* Section Abonnements */}
          <div className="space-y-6">
            <h2 className="text-lg font-bold text-[#1a2b49] border-l-4 border-[#3ab1bb] pl-4">Abonnements Actifs ({details.subscriptions.length})</h2>
            {details.subscriptions.map(sub => (
              <div key={sub.subscriptionId} className="border p-4 rounded-xl flex justify-between items-center">
                <div>
                  <p className="font-bold text-[#1a2b49]">{sub.applicationInfo.name}</p>
                </div>
                <span className="text-xs font-bold text-gray-400 bg-gray-100 px-2 py-1 rounded">{sub.throttlingPolicy}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ApiDetailsPage;