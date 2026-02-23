import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const AddSubscriptionModal = ({ isOpen, onClose, applicationId, onSubscribed }) => {
  const [availableApis, setAvailableApis] = useState([]);
  const [availablePolicies, setAvailablePolicies] = useState([]);
  const [selectedApiId, setSelectedApiId] = useState('');
  const [policy, setPolicy] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingApis, setLoadingApis] = useState(false);
  const [loadingPolicies, setLoadingPolicies] = useState(false);

  // 1. Charger et Filtrer les APIs au moment de l'ouverture
  useEffect(() => {
    if (isOpen && applicationId) {
      const fetchAndFilter = async () => {
        setLoadingApis(true);
        try {
          // Appel parallèle pour gagner du temps
          const [apisRes, appRes] = await Promise.all([
            axios.get(`${API_BASE}/apis`),
            axios.get(`${API_BASE}/applications/${applicationId}/details`)
          ]);

          const allApis = apisRes.data.apis || [];
          const currentSubscriptions = appRes.data.subscriptions || [];

          // Filtrage : Exclure les APIs déjà souscrites
          const subscribedIds = currentSubscriptions.map(sub => sub.apiId);
          const filtered = allApis.filter(api => !subscribedIds.includes(api.id));

          setAvailableApis(filtered);
        } catch (err) {
          console.error("Erreur chargement APIs:", err);
        } finally {
          setLoadingApis(false);
        }
      };

      fetchAndFilter();
    }
  }, [isOpen, applicationId]);

  // 2. Charger les politiques quand une API est sélectionnée
  useEffect(() => {
    if (selectedApiId) {
      setLoadingPolicies(true);
      axios.get(`${API_BASE}/api/v1/publisher/apis/${selectedApiId}/policies-selection`)
        .then(res => {
          // Filtrer les politiques attachées
          const attachedOnly = res.data.filter(p => p.attached === true);
          setAvailablePolicies(attachedOnly);
          
          // Sélection automatique de la première politique
          if (attachedOnly.length > 0) {
            setPolicy(attachedOnly[0].name);
          } else {
            setPolicy('');
          }
        })
        .catch(err => {
          console.error("Erreur chargement politiques:", err);
          setAvailablePolicies([]);
        })
        .finally(() => setLoadingPolicies(false));
    } else {
      setAvailablePolicies([]);
      setPolicy('');
    }
  }, [selectedApiId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const body = {
      applicationId: applicationId,
      apiId: selectedApiId, // Correction ici (était selectedApi)
      throttlingPolicy: policy,
      requestedThrottlingPolicy: policy
    };

    try {
      await axios.post(`${API_BASE}/api/v1/subscriptions`, body);
      onSubscribed(); // Callback pour rafraîchir la liste parente
      onClose();      // Fermer la modal
      setSelectedApiId(''); // Reset
    } catch (err) {
      alert(err.response?.data?.message || "Erreur lors de la souscription.");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4 text-left">
      <div className="bg-white w-full max-w-md rounded-3xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">
        <div className="bg-[#1a2b49] p-6 text-white flex justify-between items-center">
          <h3 className="font-black uppercase tracking-tight italic">Nouvel Abonnement</h3>
          <button onClick={onClose} className="text-2xl hover:text-[#3ab1bb] transition-colors">&times;</button>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          
          {/* Sélection de l'API filtrée */}
          <div className="space-y-2">
            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest block">
              API Disponible
            </label>
            <div className="relative">
              <select 
                required
                disabled={loadingApis}
                className="w-full p-4 bg-slate-50 border-2 border-transparent focus:border-[#3ab1bb] rounded-2xl outline-none font-bold text-[#1a2b49] appearance-none cursor-pointer disabled:opacity-50"
                onChange={(e) => setSelectedApiId(e.target.value)}
                value={selectedApiId}
              >
                <option value="">{loadingApis ? 'Analyse des APIs...' : 'Sélectionner une API...'}</option>
                {availableApis.map(api => (
                  <option key={api.id} value={api.id}>
                    {api.name} (v{api.version})
                  </option>
                ))}
              </select>
            </div>

            {availableApis.length === 0 && !loadingApis && (
              <div className="p-3 bg-amber-50 border border-amber-100 rounded-xl">
                <p className="text-[10px] text-amber-700 font-bold uppercase tracking-tight">
                  ⚠️ Aucune API disponible (Déjà abonné à tout)
                </p>
              </div>
            )}
          </div>

          {/* Sélection de la politique */}
          <div className="space-y-2">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest block">
              Tier de consommation
            </label>
            <select 
              disabled={!selectedApiId || loadingPolicies}
              required
              className={`w-full p-4 border-2 border-transparent focus:border-[#3ab1bb] rounded-2xl outline-none font-bold text-[#1a2b49] appearance-none ${
                  !selectedApiId ? 'bg-gray-100 text-gray-400' : 'bg-gray-50'
              }`}
              value={policy}
              onChange={(e) => setPolicy(e.target.value)}
            >
              {!selectedApiId && <option>En attente d'une API...</option>}
              {loadingPolicies && <option>Chargement des tiers...</option>}
              {!loadingPolicies && availablePolicies.map(p => (
                <option key={p.name} value={p.name}>{p.name}</option>
              ))}
            </select>
            
            {!loadingPolicies && selectedApiId && availablePolicies.length === 0 && (
              <p className="text-[10px] text-red-500 font-black mt-1 uppercase">🚫 Aucune politique active pour cette API</p>
            )}
          </div>

          <button 
            type="submit"
            disabled={loading || !selectedApiId || !policy}
            className={`w-full py-4 rounded-2xl font-black uppercase tracking-widest text-white transition-all shadow-lg ${
              loading || !selectedApiId || !policy 
              ? 'bg-gray-200 cursor-not-allowed' 
              : 'bg-[#3ab1bb] hover:bg-[#1a2b49] active:scale-95 shadow-cyan-100'
            }`}
          >
            {loading ? 'Création de l\'abonnement...' : 'Confirmer la souscription'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddSubscriptionModal;