import React, { useState } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const CreateAppModal = ({ isOpen, onClose, onAppCreated }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    throttlingPolicy: 'Unlimited',
    tokenType: 'JWT'
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Appel à ton endpoint /create-full
      await axios.post(`${API_BASE}/applications/create-full`, formData);
      setFormData({ name: '', description: '', throttlingPolicy: 'Unlimited', tokenType: 'JWT' });
      onAppCreated(); // Rafraîchir la liste
      onClose();      // Fermer le modal
    } catch (err) {
      console.error(err);
      alert("Erreur lors de la création de l'application.");
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
            <h2 className="text-2xl font-black uppercase tracking-tighter">Nouvelle Application</h2>
            <button onClick={onClose} className="text-3xl hover:text-[#3ab1bb] transition-colors">&times;</button>
          </div>
          <p className="text-[#3ab1bb] text-xs font-bold uppercase tracking-widest">Enregistrement & Génération de clés</p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 flex-grow space-y-10 overflow-y-auto">
          
          {/* Nom de l'application */}
          <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] transition-all py-2">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1 block">Nom de l'application *</label>
            <input 
              required
              className="w-full bg-transparent outline-none text-[#1a2b49] font-bold text-lg"
              placeholder="Ex: MaPlateformeInterne"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>

          {/* Description */}
          <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] transition-all py-2">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1 block">Description</label>
            <textarea 
              className="w-full bg-transparent outline-none text-[#1a2b49] text-sm resize-none"
              placeholder="À quoi sert cette application ?"
              rows="2"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
            />
          </div>

          {/* Politique de quota (Throttling) */}
          <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] transition-all py-2">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1 block">Quota de requêtes (Throttling)</label>
            <select 
              className="w-full bg-transparent outline-none text-[#1a2b49] font-bold cursor-pointer"
              value={formData.throttlingPolicy}
              onChange={(e) => setFormData({...formData, throttlingPolicy: e.target.value})}
            >
              <option value="Unlimited">Unlimited (Illimité)</option>
              <option value="Bronze">Bronze (1000 req/min)</option>
              <option value="Silver">Silver (2000 req/min)</option>
              <option value="Gold">Gold (5000 req/min)</option>
            </select>
          </div>

          {/* Type de Token */}
          <div className="border-b-2 border-gray-100 focus-within:border-[#3ab1bb] transition-all py-2">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1 block">Type de Sécurité (Token)</label>
            <select 
              className="w-full bg-transparent outline-none text-[#1a2b49] font-bold cursor-pointer"
              value={formData.tokenType}
              onChange={(e) => setFormData({...formData, tokenType: e.target.value})}
            >
              <option value="JWT">JWT (Recommandé)</option>
              <option value="OAuth2">OAuth2 Standard</option>
            </select>
          </div>

          <button 
            type="submit"
            disabled={loading}
            className={`w-full py-5 rounded-2xl font-black uppercase tracking-widest text-white shadow-xl transition-all ${
              loading ? 'bg-gray-300' : 'bg-[#3ab1bb] hover:bg-[#1a2b49] shadow-cyan-100'
            }`}
          >
            {loading ? 'Création en cours...' : 'Créer l\'application →'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateAppModal;