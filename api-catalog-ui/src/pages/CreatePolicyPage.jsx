import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CreatePolicyPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [policies, setPolicies] = useState([]);
  const [fetching, setFetching] = useState(true);
  
  const [formData, setFormData] = useState({
    policyName: '',
    description: '',
    requestCount: 10,
    timeUnit: 'min',
    unitTime: 1
  });

  // Récupérer toutes les politiques existantes
  const fetchPolicies = async () => {
    try {
      setFetching(true);
      const response = await axios.get('http://localhost:8080/api/v1/publisher/apis/all-policies');
      setPolicies(response.data);
    } catch (err) {
      console.error("Erreur lors de la récupération des politiques:", err);
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    fetchPolicies();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await axios.post('http://localhost:8080/api/v1/admin/policies', formData);
      
      if (response.status === 201 || response.status === 200) {
        toast.success("Politique créée avec succès !");
        setFormData({ policyName: '', description: '', requestCount: 10, timeUnit: 'min', unitTime: 1 });
        fetchPolicies(); // Rafraîchir la liste après création
      }
    } catch (err) {
      console.error("Erreur création politique:", err);
      toast.error(err.response?.data?.description || "Erreur serveur");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-[1400px] mx-auto p-8 animate-in fade-in duration-500 space-y-12 pb-24">
      
      {/* --- SECTION FORMULAIRE --- */}
      <div className="space-y-8">
        <div className="mb-6">
          <h1 className="text-4xl font-black text-[#1a2b49] tracking-tight">
            Configuration <span className="text-[#3ab1bb]">Throttling</span>
          </h1>
          <p className="text-slate-400 mt-2 font-medium">Créez de nouveaux paliers de consommation pour vos APIs.</p>
        </div>

        <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Identifiants */}
          <div className="space-y-6 bg-white p-8 rounded-[2rem] border shadow-sm">
            <div>
              <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Nom (ID unique)</label>
              <input 
                required
                type="text"
                className="w-full p-4 bg-slate-50 border-transparent border-b-2 border-b-slate-100 focus:border-b-[#3ab1bb] focus:bg-white outline-none transition-all font-bold text-[#1a2b49]"
                placeholder="Ex: GoldTier"
                value={formData.policyName}
                onChange={(e) => {
                  const cleanedValue = e.target.value.replace(/[^a-zA-Z0-9]/g, '');
                  setFormData({...formData, policyName: cleanedValue});
                }}
              />
              <p className="text-[9px] text-slate-400 mt-1 uppercase font-bold tracking-tighter">Lettres et chiffres uniquement - pas d'espaces</p>
            </div>

            <div>
              <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest block mb-2">Description</label>
              <textarea 
                rows="3"
                className="w-full p-4 bg-slate-50 border rounded-2xl focus:border-[#3ab1bb] focus:bg-white outline-none transition-all text-sm"
                placeholder="A quoi sert cette politique ?"
                value={formData.description}
                onChange={(e) => setFormData({...formData, description: e.target.value})}
              />
            </div>
          </div>

          {/* Quotas */}
          <div className="space-y-6 bg-[#1a2b49] p-8 rounded-[2rem] text-white shadow-xl">
            <div className="grid grid-cols-1 gap-4">
              <div>
                <label className="text-[10px] font-black text-slate-300 uppercase tracking-widest block mb-2">Nombre de requêtes</label>
                <input 
                  required
                  type="number"
                  className="w-full p-4 bg-white/10 border-b-2 border-white/20 focus:border-[#3ab1bb] outline-none transition-all font-black text-3xl text-[#3ab1bb]"
                  value={formData.requestCount}
                  onChange={(e) => setFormData({...formData, requestCount: parseInt(e.target.value) || 0})}
                />
              </div>
              
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-[10px] font-black text-slate-300 uppercase tracking-widest block mb-2">Unité</label>
                  <select 
                    className="w-full p-4 bg-white/10 border rounded-xl focus:border-[#3ab1bb] outline-none font-bold text-sm"
                    value={formData.timeUnit}
                    onChange={(e) => setFormData({...formData, timeUnit: e.target.value})}
                  >
                    <option value="min" className="text-black">Minutes</option>
                    <option value="hour" className="text-black">Heures</option>
                    <option value="day" className="text-black">Jours</option>
                  </select>
                </div>
                <div>
                  <label className="text-[10px] font-black text-slate-300 uppercase tracking-widest block mb-2">Valeur durée</label>
                  <input 
                    required
                    type="number"
                    className="w-full p-4 bg-white/10 border rounded-xl focus:border-[#3ab1bb] outline-none font-bold text-sm"
                    value={formData.unitTime}
                    onChange={(e) => setFormData({...formData, unitTime: parseInt(e.target.value) || 1})}
                  />
                </div>
              </div>
            </div>

            <button 
              type="submit"
              disabled={loading}
              className="w-full py-4 mt-4 bg-[#3ab1bb] hover:bg-white hover:text-[#1a2b49] text-white rounded-2xl font-black uppercase tracking-widest transition-all shadow-lg active:scale-95 disabled:opacity-50"
            >
              {loading ? "Synchronisation WSO2..." : "Enregistrer la Politique"}
            </button>
          </div>
        </form>
      </div>

      <hr className="border-slate-100" />

      {/* --- SECTION LISTE (TABLEAU) --- */}
      <div className="space-y-6">
        <div className="flex justify-between items-end">
          <div>
            <h2 className="text-2xl font-black text-[#1a2b49] uppercase tracking-tighter">Politiques Actives</h2>
            <p className="text-slate-400 text-sm">Liste des paliers synchronisés avec l'API Manager.</p>
          </div>
          <div className="text-right">
            <span className="text-[10px] font-black text-slate-400 uppercase block">Total</span>
            <span className="text-2xl font-black text-[#3ab1bb]">{policies.length}</span>
          </div>
        </div>

        <div className="bg-white border rounded-[2rem] overflow-hidden shadow-sm">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/50 border-b border-slate-100">
                <th className="p-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Policy Name</th>
                <th className="p-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Description</th>
                <th className="p-6 text-[10px] font-black text-slate-400 uppercase tracking-widest">Quota</th>
                <th className="p-6 text-[10px] font-black text-slate-400 uppercase tracking-widest text-center">Intervalle</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
              {fetching ? (
                <tr>
                  <td colSpan="4" className="p-10 text-center text-slate-400 font-medium">Chargement des données...</td>
                </tr>
              ) : policies.map((p, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50 transition-colors group">
                  <td className="p-6">
                    <span className="font-bold text-[#1a2b49] group-hover:text-[#3ab1bb] transition-colors">{p.policyName}</span>
                  </td>
                  <td className="p-6 text-sm text-slate-500 max-w-md truncate">
                    {p.description || <span className="italic text-slate-300">Aucune description</span>}
                  </td>
                  <td className="p-6">
                    <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs font-black border border-blue-100">
                      {p.requestCount === 2147483647 ? '∞' : p.requestCount.toLocaleString()} req
                    </span>
                  </td>
                  <td className="p-6 text-center text-xs font-bold text-slate-600">
                    {p.unitTime} {p.timeUnit}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default CreatePolicyPage;