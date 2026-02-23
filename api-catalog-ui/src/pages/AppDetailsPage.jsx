import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import AddSubscriptionModal from '../components/AddSubscriptionModal';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const AppDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchDetails = () => {
    axios.get(`${API_BASE}/applications/${id}/details`)
      .then(res => setDetails(res.data))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  if (!details) return <div className="p-20 text-center">Chargement des credentials...</div>;

  return (
    <div className="max-w-5xl mx-auto animate-in slide-in-from-bottom-4 duration-500 pb-20">
      <button onClick={() => navigate(-1)} className="text-[#3ab1bb] font-bold mb-6 flex items-center hover:underline">
        ← Retour aux applications
      </button>

      <div className="bg-white rounded-[2.5rem] shadow-xl overflow-hidden border border-gray-100">
        {/* Header */}
        <div className="bg-[#1a2b49] p-10 text-white">
          <div className="flex justify-between items-center">
            <div>
              <p className="text-[#3ab1bb] font-bold text-xs uppercase tracking-[0.3em] mb-2">Details de l'application</p>
              <h1 className="text-4xl font-black uppercase">{details.name}</h1>
            </div>
            <div className="text-right flex flex-col items-end gap-2">
              <span className="bg-[#3ab1bb] px-4 py-2 rounded-lg font-bold text-sm">STATUS: {details.status}</span>
            </div>
          </div>
        </div>

        <div className="p-10">
          {/* Section Keys */}
          <div className="grid grid-cols-1 gap-6 mb-12">
            <h2 className="text-xl font-bold text-[#1a2b49] mb-2 border-l-4 border-[#3ab1bb] pl-4">Production Keys</h2>
            <div className="bg-gray-50 p-6 rounded-2xl border border-dashed border-gray-300">
              <div className="mb-4">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Consumer Key</label>
                <div className="flex gap-2 mt-1">
                    <p className="font-mono text-sm text-slate-700 bg-white p-2 flex-grow rounded border">{details.consumerKey}</p>
                    <button onClick={() => navigator.clipboard.writeText(details.consumerKey)} className="text-xs text-[#3ab1bb] font-bold">COPIER</button>
                </div>
              </div>
              <div>
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Consumer Secret</label>
                <p className="font-mono text-sm text-slate-700 bg-white p-2 mt-1 rounded border">••••••••••••••••••••••••</p>
              </div>
            </div>
          </div>

          {/* Section APIs Souscrites */}
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-[#1a2b49] border-l-4 border-[#3ab1bb] pl-4">APIs liées à cette application</h2>
            <button 
              onClick={() => setIsModalOpen(true)}
              className="bg-[#1a2b49] text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-[#3ab1bb] transition-colors"
            >
              + S'ABBONNER À UNE API
            </button>   
          </div>

          <div className="space-y-4">
            {details.subscriptions.length > 0 ? (
              details.subscriptions.map(sub => (
                <div key={sub.subscriptionId} className="flex justify-between items-center p-6 bg-white border rounded-2xl hover:border-[#3ab1bb] transition-all shadow-sm group">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center font-bold text-[#1a2b49] group-hover:bg-[#3ab1bb]/10 transition-colors">
                      {sub.apiInfo.name[0]}
                    </div>
                    <div>
                      <h4 className="font-bold text-[#1a2b49]">{sub.apiInfo.name}</h4>
                      <p className="text-xs text-gray-500">{sub.apiInfo.context} - v{sub.apiInfo.version}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="text-[10px] font-black bg-blue-50 text-blue-600 px-3 py-1 rounded-full uppercase italic">{sub.throttlingPolicy}</span>
                    <p className="text-[10px] mt-1 text-green-500 font-bold uppercase">{sub.status}</p>
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-10 bg-gray-50 rounded-2xl border-2 border-dashed">
                <p className="text-gray-400 italic">Aucune souscription active pour le moment.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <AddSubscriptionModal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        applicationId={id}
        onSubscribed={fetchDetails}
      />
    </div>
  );
};

export default AppDetailsPage;