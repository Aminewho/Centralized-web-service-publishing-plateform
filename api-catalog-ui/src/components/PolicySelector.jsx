import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Mapping des descriptions (WSO2 standard) pour le look de la photo
const POLICY_DESCRIPTIONS = {
  Bronze: "Allows 1000 requests per minute",
  Silver: "Allows 2000 requests per minute",
  Gold: "Allows 5000 requests per minute",
  Unlimited: "Allows unlimited requests",
  DefaultSubscriptionless: "Default policy without subscriptions"
};

const PolicySelector = ({ apiId }) => {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  // 1. Charger l'état actuel (attached: true/false)
  useEffect(() => {
    fetchPolicies();
  }, [apiId]);

  const fetchPolicies = async () => {
    try {
      const res = await axios.get(`${API_BASE}/api/v1/publisher/apis/${apiId}/policies-selection`);
      setPolicies(res.data);
    } catch (err) {
      console.error("Erreur chargement politiques:", err);
    } finally {
      setLoading(false);
    }
  };

  // 2. Gérer le changement des cases à cocher
  const handleToggle = (policyName) => {
    setPolicies(prev => prev.map(p => 
      p.name === policyName ? { ...p, attached: !p.attached } : p
    ));
  };

  // 3. Sauvegarder (PUT)
  const handleUpdate = async () => {
    setUpdating(true);
    // On extrait uniquement les noms des politiques qui sont "attached: true"
    const selectedPolicies = policies
      .filter(p => p.attached)
      .map(p => p.name);

    try {
      await axios.put(`${API_BASE}/api/v1/publisher/apis/${apiId}/policies`, {
        policies: selectedPolicies
      });
      alert("Politiques mises à jour avec succès !");
    } catch (err) {
      alert("Erreur lors de la mise à jour.");
    } finally {
      setUpdating(false);
    }
  };

  if (loading) return <div className="text-sm text-gray-400">Chargement des politiques...</div>;

  return (
    <div className="bg-white rounded-xl border border-gray-100 p-6 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-[#1a2b49] font-bold uppercase tracking-widest text-xs">
          Politiques de souscription disponibles
        </h3>
        <button 
          onClick={handleUpdate}
          disabled={updating}
          className="text-[10px] font-black bg-[#3ab1bb] text-white px-4 py-2 rounded-lg hover:bg-[#1a2b49] transition-all disabled:bg-gray-200"
        >
          {updating ? "SYNCHRONISATION..." : "METTRE À JOUR"}
        </button>
      </div>

      <div className="space-y-4">
        {policies.map((policy) => (
          <div key={policy.name} className="flex items-center group cursor-pointer" onClick={() => handleToggle(policy.name)}>
            {/* Checkbox stylisé */}
            <div className={`w-5 h-5 border-2 rounded flex items-center justify-center transition-all ${
              policy.attached ? 'bg-[#3ab1bb] border-[#3ab1bb]' : 'border-gray-300 bg-white'
            }`}>
              {policy.attached && (
                <svg className="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" d="M5 13l4 4L19 7" />
                </svg>
              )}
            </div>

            {/* Label et Description */}
            <div className="ml-4 flex items-center space-x-2">
              <span className={`text-sm font-bold ${policy.attached ? 'text-[#1a2b49]' : 'text-gray-500'}`}>
                {policy.name}
              </span>
              <span className="text-gray-300">:</span>
            
              
              {/* Icône Info grise */}
              <div className="w-4 h-4 rounded-full bg-gray-200 flex items-center justify-center text-[10px] text-white font-bold cursor-help ml-2">
                i
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PolicySelector;