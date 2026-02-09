import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
// Base API URL (configurable via Vite env var VITE_API_BASE_URL)
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const AppDetailsPage = () => {
  const { id } = useParams();
  const [details, setDetails] = useState(null);

  useEffect(() => {
    axios.get(`${API_BASE}/applications/${id}/details`)
      .then(res => setDetails(res.data))
      .catch(err => console.error(err));
  }, [id]);

  if (!details) return <div className="p-20 text-center">Chargement des credentials...</div>;

  return (
    <div className="max-w-5xl mx-auto animate-in slide-in-from-bottom-4 duration-500">
      <div className="bg-white rounded-[2.5rem] shadow-xl overflow-hidden border border-gray-100">
        {/* Header de l'App */}
        <div className="bg-[#1a2b49] p-10 text-white">
          <div className="flex justify-between items-center">
            <div>
              <p className="text-[#3ab1bb] font-bold text-xs uppercase tracking-[0.3em] mb-2">Details de l'application</p>
              <h1 className="text-4xl font-black uppercase">{details.name}</h1>
            </div>
            <div className="text-right">
              <span className="bg-[#3ab1bb] px-4 py-2 rounded-lg font-bold text-sm">STATUS: {details.status}</span>
            </div>
          </div>
        </div>

        <div className="p-10">
          {/* Section Keys - Très important pour le RNE */}
          <div className="grid grid-cols-1 gap-6 mb-12">
            <h2 className="text-xl font-bold text-[#1a2b49] mb-2">Production Keys</h2>
            <div className="bg-gray-50 p-6 rounded-2xl border border-dashed border-gray-300">
              <div className="mb-4">
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Consumer Key</label>
                <p className="font-mono text-sm text-slate-700 bg-white p-2 mt-1 rounded border">{details.consumerKey}</p>
              </div>
              <div>
                <label className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Consumer Secret</label>
                <p className="font-mono text-sm text-slate-700 bg-white p-2 mt-1 rounded border">••••••••••••••••••••••••</p>
              </div>
            </div>
          </div>

          {/* Section APIs Souscrites */}
          <h2 className="text-xl font-bold text-[#1a2b49] mb-6">APIs liées à cette application</h2>
          <div className="space-y-4">
            {details.subscriptions.map(sub => (
              <div key={sub.subscriptionId} className="flex justify-between items-center p-6 bg-white border rounded-2xl hover:border-[#3ab1bb] transition-all shadow-sm">
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center font-bold text-[#1a2b49]">{sub.apiInfo.name[0]}</div>
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
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AppDetailsPage;