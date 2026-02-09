import React, { useState } from 'react';
import axios from 'axios';

// Base API URL (configurable via Vite env var VITE_API_BASE_URL)
const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const CreateApiModal = ({ isOpen, onClose, onApiCreated }) => {
  const [formData, setFormData] = useState({
    name: '',
    context: '',
    version: '',
    endpoint: ''
  });
  
  // État pour les variables de chemin (target)
  const [pathVariables, setPathVariables] = useState([]);
  const [loading, setLoading] = useState(false);

  // Ajouter un nouvel input pour une variable
  const addVariable = () => {
    setPathVariables([...pathVariables, '']);
  };

  // Modifier le nom d'une variable spécifique
  const handleVariableChange = (index, value) => {
    const updatedVars = [...pathVariables];
    updatedVars[index] = value;
    setPathVariables(updatedVars);
  };

  // Supprimer une variable
  const removeVariable = (index) => {
    setPathVariables(pathVariables.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Construction du target : si pas de variables, on met /*
    // Sinon on construit /{var1}/{var2}...
    const targetPath = pathVariables.length > 0 
      ? '/' + pathVariables.map(v => `{${v}}`).join('/')
      : '/*';

    const body = {
      name: formData.name,
      context: formData.context,
      version: "1.0.0", // Version par défaut, peut être modifiée pour être un champ du formulaire
      endpointConfig: {
        endpoint_type: "http",
        production_endpoints: { url: formData.endpoint },
        sandbox_endpoints: { url: formData.endpoint }
      },
      operations: [{
        target: targetPath,
        verb: "GET",
        authType: "Application & Application User",
        throttlingPolicy: "Unlimited"
      }]
    };

    try {
        console.log("Données envoyées pour création API :", body);
  await axios.post(`${API_BASE}/apis/create-and-publish`, body);
      // Reset
      setFormData({ name: '', context: '', version: '', endpoint: '' });
      setPathVariables([]);
      onApiCreated();
      onClose();
    } catch (err) {
      alert("Erreur de publication. Vérifiez la console.");
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
            <h2 className="text-2xl font-black uppercase">Configuration API</h2>
            <button onClick={onClose} className="text-3xl">&times;</button>
          </div>
          <p className="text-[#3ab1bb] text-xs font-bold uppercase tracking-widest">Publication WSO2</p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 flex-grow overflow-y-auto space-y-8">
          
          {/* Champs obligatoires standards */}
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
              <input required type="url" className="w-full bg-transparent outline-none text-sm font-mono" value={formData.endpoint} onChange={(e) => setFormData({...formData, endpoint: e.target.value})} />
            </div>
          </div>

          {/* SECTION DYNAMIQUE : TARGET VARIABLES */}
          <div className="space-y-4 pt-4">
            <div className="flex justify-between items-center">
              <label className="text-[10px] font-black text-[#1a2b49] uppercase tracking-widest">Variables de chemin (URL)</label>
              <button 
                type="button"
                onClick={addVariable}
                className="text-[#3ab1bb] text-xs font-bold hover:underline"
              >
                + AJOUTER UNE VARIABLE
              </button>
            </div>

            {pathVariables.length === 0 ? (
              <p className="text-xs text-gray-400 italic bg-gray-50 p-3 rounded">Par défaut : /* (Toutes les ressources)</p>
            ) : (
              <div className="space-y-3">
                {pathVariables.map((variable, index) => (
                  <div key={index} className="flex items-center gap-2 animate-in fade-in slide-in-from-left-2">
                    <span className="text-gray-300 font-mono">/{"{"}</span>
                    <input 
                      required
                      placeholder="nom_variable"
                      className="flex-grow border-b border-gray-200 outline-none text-sm font-bold text-[#3ab1bb]"
                      value={variable}
                      onChange={(e) => handleVariableChange(index, e.target.value)}
                    />
                    <span className="text-gray-300 font-mono">{"}"}</span>
                    <button 
                      type="button" 
                      onClick={() => removeVariable(index)}
                      className="text-red-400 text-xs hover:text-red-600 px-2"
                    >
                      Supprimer
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button 
            type="submit"
            disabled={loading}
            className={`w-full py-4 rounded-xl font-black uppercase text-white shadow-lg transition-all ${
              loading ? 'bg-gray-300' : 'bg-[#3ab1bb] hover:bg-[#1a2b49]'
            }`}
          >
            {loading ? 'Publication...' : 'Publier sur le portail →'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateApiModal;