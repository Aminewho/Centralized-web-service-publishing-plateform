import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const CreateApiModal = ({ isOpen, onClose, onApiCreated }) => {
  const [formData, setFormData] = useState({
    name: '',
    context: '',
    version: '1.0.0',
    endpoint: ''
  });
  
  const [pathVariables, setPathVariables] = useState([]);
  const [policies, setPolicies] = useState([]); // Liste des politiques API
  const [selectedPolicy, setSelectedPolicy] = useState(null); // Politique choisie
  const [loading, setLoading] = useState(false);

  // 1. Charger les politiques au montage de la modal
  useEffect(() => {
    if (isOpen) {
      axios.get(`${API_BASE}/api/v1/publisher/apis/policies`)
        .then(res => setPolicies(res.data))
        .catch(err => console.error("Erreur lors du chargement des politiques:", err));
    }
  }, [isOpen]);

  const addVariable = () => setPathVariables([...pathVariables, '']);

  const handleVariableChange = (index, value) => {
    const updatedVars = [...pathVariables];
    updatedVars[index] = value;
    setPathVariables(updatedVars);
  };

  const removeVariable = (index) => setPathVariables(pathVariables.filter((_, i) => i !== index));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const targetPath = pathVariables.length > 0 
      ? '/' + pathVariables.map(v => `{${v}}`).join('/')
      : '/*';

    // Construction du body avec la politique d'opération
    const body = {
      name: formData.name,
      context: formData.context,
      version: formData.version,
      endpointConfig: {
        endpoint_type: "http",
        production_endpoints: { url: formData.endpoint },
        sandbox_endpoints: { url: formData.endpoint }
      },
      operations: [{
        target: targetPath,
        verb: "GET",
        authType: "Application & Application User",
        throttlingPolicy: "Unlimited",
        // Ajout de la politique sélectionnée ici
        operationPolicies: {
          request: [],
          response: selectedPolicy ? [
            {
              policyName: selectedPolicy.name,
              policyVersion: selectedPolicy.version,
              policyId: selectedPolicy.id,
              parameters: {}
            }
          ] : [],
          fault: []
        }
      }]
    };

    try {
      await axios.post(`${API_BASE}/apis/create-and-publish`, body);
      setFormData({ name: '', context: '', version: '1.0.0', endpoint: '' });
      setPathVariables([]);
      setSelectedPolicy(null);
      onApiCreated();
      onClose();
    } catch (err) {
      alert("Erreur de publication. Vérifiez la console.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-end bg-black/50 backdrop-blur-sm">
      <div className="h-full w-full max-w-lg bg-white shadow-2xl animate-in slide-in-from-right duration-300 flex flex-col">
        
        <div className="p-8 bg-[#1a2b49] text-white">
          <div className="flex justify-between items-center mb-2">
            <h2 className="text-2xl font-black uppercase tracking-tighter">Configuration API</h2>
            <button onClick={onClose} className="text-3xl hover:text-[#3ab1bb]">&times;</button>
          </div>
          <p className="text-[#3ab1bb] text-xs font-bold uppercase tracking-widest">Publication WSO2 APK</p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 flex-grow overflow-y-auto space-y-8">
          
          <div className="space-y-6">
            <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] py-2">
              <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Nom de l'API *</label>
              <input required className="w-full bg-transparent outline-none text-[#1a2b49] font-bold" value={formData.name} onChange={(e) => setFormData({...formData, name: e.target.value})} />
            </div>

            <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] py-2">
              <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest">Contexte *</label>
              <input required className="w-full bg-transparent outline-none text-[#1a2b49] font-bold" value={formData.context} onChange={(e) => setFormData({...formData, context: e.target.value})} />
            </div>

            <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] py-2">
              <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest">URL Backend *</label>
              <input required type="url" className="w-full bg-transparent outline-none text-sm font-mono text-slate-600" value={formData.endpoint} onChange={(e) => setFormData({...formData, endpoint: e.target.value})} />
            </div>
          </div>

          {/* SÉLECTEUR DE POLITIQUE (NOUVEAU) */}
          <div className="space-y-3 bg-slate-50 p-4 rounded-2xl border border-slate-100">
            <label className="text-[10px] font-black text-[#1a2b49] uppercase tracking-widest block">
              Politique d'opération (Response Flow)
            </label>
            <select 
              className="w-full p-3 bg-white border border-slate-200 rounded-xl outline-none text-sm font-bold text-slate-700 focus:border-[#3ab1bb]"
              onChange={(e) => {
                const policy = policies.find(p => p.id === e.target.value);
                setSelectedPolicy(policy);
              }}
              value={selectedPolicy?.id || ""}
            >
              <option value="">Aucune politique sélectionnée</option>
              {policies.map(p => (
                <option key={p.id} value={p.id}>
                  {p.displayName} ({p.version})
                </option>
              ))}
            </select>
            <p className="text-[9px] text-slate-400 font-medium">
              Cette politique sera appliquée au flux de réponse de l'opération GET.
            </p>
          </div>

          {/* VARIABLES DE CHEMIN */}
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <label className="text-[10px] font-black text-[#1a2b49] uppercase tracking-widest">Ressource dynamique</label>
              <button type="button" onClick={addVariable} className="text-[#3ab1bb] text-[10px] font-black hover:underline uppercase">+ Ajouter</button>
            </div>

            {pathVariables.length === 0 ? (
              <p className="text-[11px] text-gray-400 italic bg-gray-50 p-4 rounded-xl border border-dashed">Route par défaut : /*</p>
            ) : (
              <div className="space-y-3">
                {pathVariables.map((variable, index) => (
                  <div key={index} className="flex items-center gap-2 animate-in fade-in slide-in-from-left-2">
                    <span className="text-gray-300 font-mono">/{"{"}</span>
                    <input 
                      required
                      placeholder="id, name..."
                      className="flex-grow border-b border-gray-200 outline-none text-sm font-bold text-[#3ab1bb] bg-transparent"
                      value={variable}
                      onChange={(e) => handleVariableChange(index, e.target.value)}
                    />
                    <span className="text-gray-300 font-mono">{"}"}</span>
                    <button type="button" onClick={() => removeVariable(index)} className="text-red-400 hover:text-red-600">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button 
            type="submit"
            disabled={loading}
            className={`w-full py-5 rounded-2xl font-black uppercase tracking-widest text-white shadow-xl transition-all ${
              loading ? 'bg-slate-200 cursor-wait' : 'bg-[#3ab1bb] hover:bg-[#1a2b49] active:scale-95'
            }`}
          >
            {loading ? 'Publication en cours...' : 'Déployer sur APK Gateway'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateApiModal;