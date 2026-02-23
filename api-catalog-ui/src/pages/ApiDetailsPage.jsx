import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import PolicySelector from '../components/PolicySelector';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const ApiDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState(null);
  
  // States for Authentication
  const [selectedAppId, setSelectedAppId] = useState('');
  const [accessToken, setAccessToken] = useState('');
  const [loadingToken, setLoadingToken] = useState(false);

  // States for Dynamic Operations
  const [opInputs, setOpInputs] = useState({}); // Stores { "opId": { "varName": "value" } }
  const [testResults, setTestResults] = useState({}); // Stores { "opId": responseData }
  const [executingOp, setExecutingOp] = useState(null); // Tracks which specific ID is loading

  useEffect(() => {
    axios.get(`${API_BASE}/api/v1/publisher/apis/${id}/full-details`)
      .then(res => {
        setDetails(res.data);
      })
      .catch(err => console.error(err));
  }, [id]);

  const handleGenerateToken = async () => {
    if (!selectedAppId) return alert("Veuillez sélectionner une application d'abord");
    setLoadingToken(true);
    try {
      const appDetailsRes = await axios.get(`${API_BASE}/applications/${selectedAppId}/details`);
      const { consumerKey, consumerSecret } = appDetailsRes.data;

      const tokenResponse = await axios.post(`${API_BASE}/apis/generate-token`, {
        consumerKey,
        consumerSecret
      });
      setAccessToken(tokenResponse.data.access_token);
    } catch (err) {
      alert("Erreur lors de la génération du token.");
    } finally {
      setLoadingToken(false);
    }
  };

const executeOperationTest = async (operation, opIndex) => {
  const opId = `op-${opIndex}`;
  setExecutingOp(opId);

  // Construction de l'URL
  let baseUrl = details.gatewayUrl || `https://localhost:8243/${details.context}/${details.version}`;
  baseUrl = baseUrl.replace(/\/$/, "");
  let path = operation.target.startsWith('/') ? operation.target : '/' + operation.target;

  // Remplacement des variables {id}, etc.
  const matches = path.match(/\{([^}]+)\}/g) || [];
  matches.forEach(match => {
    const varName = match.replace(/[{}]/g, '');
    const value = opInputs[opId]?.[varName] || match;
    path = path.replace(match, value);
  });

  const finalUrl = `${baseUrl}${path}`;

  try {
    const res = await axios({
      method: operation.verb,
      url: finalUrl,
      headers: { 
        'Authorization': `Bearer ${accessToken}`,
        'Accept': 'application/json'
      }
    });
    
    // Succès (200, 201, etc.)
    setTestResults(prev => ({ ...prev, [opId]: res.data }));

  } catch (err) {
    let errorData;

    if (err.response) {
      // LE SERVEUR A RÉPONDU (404, 429, 500, etc.)
      // On récupère le corps exact de l'erreur envoyé par le serveur
      errorData = err.response.data || {
        status: err.response.status,
        statusText: err.response.statusText,
        message: "Le serveur a renvoyé une erreur sans corps de message."
      };
      
      // Si c'est une 404 et qu'il n'y a pas de data, on clarifie
      if (err.response.status === 404 && !err.response.data) {
        errorData = { error: 404, message: `La ressource à l'adresse ${path} est introuvable.` };
      }

    } else if (err.request) {
      // PAS DE RÉPONSE (Problème réseau ou CORS)
      errorData = {
        error: "Network Error",
        message: "Aucune réponse du serveur. Vérifiez votre connexion ou les politiques CORS.",
        endpoint: finalUrl
      };
    } else {
      // ERREUR DE CONFIGURATION JS
      errorData = { error: "Request Error", message: err.message };
    }

    setTestResults(prev => ({ ...prev, [opId]: errorData }));
  } finally {
    setExecutingOp(null);
  }
};

  if (!details) return <div className="text-center py-20">Chargement...</div>;

return (
    <div className="animate-in slide-in-from-bottom-4 duration-500 space-y-8 pb-20 p-8 max-w-[1600px] mx-auto">
      
      {/* --- SECTION 1: HEADER DYNAMIQUE --- */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 border-b border-slate-100 pb-10">
        <div className="space-y-4">
          <button 
            onClick={() => navigate(-1)} 
            className="group flex items-center gap-2 text-[10px] font-black text-[#3ab1bb] uppercase tracking-widest hover:text-[#1a2b49] transition-colors"
          >
            <span className="group-hover:-translate-x-1 transition-transform">←</span> Retour au Catalogue
          </button>
          
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 bg-gradient-to-br from-[#1a2b49] to-[#3ab1bb] rounded-2xl flex items-center justify-center text-white text-2xl font-black shadow-lg shadow-cyan-100">
              {details.name?.charAt(0).toUpperCase()}
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-4xl font-black text-[#1a2b49] tracking-tight">
                  {details.name}
                </h1>
                <span className="bg-slate-100 text-slate-500 text-[10px] font-black px-2 py-1 rounded-md uppercase border border-slate-200">
                  v{details.version}
                </span>
              </div>
              <p className="text-slate-400 font-medium mt-1">
                Context: <span className="font-mono text-[#3ab1bb] font-bold">{details.context}</span> • 
              </p>
            </div>
          </div>
        </div>

        {/* Statistiques de l'API */}
        <div className="flex items-center gap-4">
          <div className="bg-white p-4 rounded-2xl border shadow-sm min-w-[140px] text-center">
            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Abonnés</p>
            <p className="text-2xl font-black text-[#1a2b49]">
              {details.subscriptions?.length || 0}
              <span className="text-[10px] text-slate-300 ml-1 font-normal uppercase">Apps</span>
            </p>
          </div>
          <div className="bg-emerald-50 p-4 rounded-2xl border border-emerald-100 min-w-[140px] text-center">
            <p className="text-[10px] font-black text-emerald-600 uppercase tracking-widest mb-1">Status</p>
            <div className="flex items-center justify-center gap-2">
              <span className="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
              <p className="text-sm font-black text-emerald-700 uppercase">Published</p>
            </div>
          </div>
        </div>
      </div>

      {/* --- SECTION 2: GRILLE PRINCIPALE --- */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        
        {/* COLONNE GAUCHE (Configurations & Politiques) - 4/12 */}
        <div className="lg:col-span-4 space-y-8">
          <section className="bg-white rounded-3xl p-6 border shadow-sm">
            <h2 className="text-sm font-black text-[#1a2b49] uppercase tracking-tighter mb-6 flex items-center gap-2">
              <div className="w-1.5 h-1.5 bg-[#3ab1bb] rounded-full"></div>
              Infrastructure Endpoints
            </h2>
            
            <div className="space-y-6">
              {/* Gateway (Point d'entrée public) */}
              <div>
                <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest"> Public URL</label>
                <div className="mt-2 p-3 bg-slate-50 rounded-xl font-mono text-[11px] break-all border text-slate-600">
                  {details.gatewayUrl || `https://localhost:8243/${details.context}/${details.version}`}
                </div>
              </div>

              {/* Production Backend URL (Celle que vous avez demandée) */}
               <div>
                <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest"> Backend URL</label>
                <div className="mt-2 p-3 bg-slate-50 rounded-xl font-mono text-[11px] break-all border text-slate-600">
                  {details.endpointConfig?.production_endpoints?.url || "Cible HTTP Non configurée"}
               </div>
              </div>
            </div>
          </section>

          <section>
            <PolicySelector apiId={id} />
          </section>
        </div>

        {/* COLONNE DROITE (Console & Applications) - 8/12 */}
        <div className="lg:col-span-8 space-y-8">
          
          {/* A. CONSOLE DE TEST */}
          <div className="bg-slate-50 rounded-[2rem] p-8 border border-slate-200 shadow-inner">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
              <h2 className="text-xl font-black text-[#1a2b49] flex items-center gap-3">
                <span className="relative flex h-3 w-3">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-cyan-500"></span>
                </span>
                API CONSOLE
              </h2>

              <div className="flex items-center gap-2 w-full sm:w-auto">
                <select 
                  className="flex-1 sm:flex-none p-2 bg-white border rounded-lg text-xs font-bold outline-none focus:border-[#3ab1bb] min-w-[160px]"
                  value={selectedAppId}
                  onChange={(e) => setSelectedAppId(e.target.value)}
                >
                  <option value="">Sélectionner App...</option>
                  {details.subscriptions?.map(sub => (
                    <option key={sub.subscriptionId} value={sub.applicationInfo.applicationId}>
                      {sub.applicationInfo.name}
                    </option>
                  ))}
                </select>
                <button 
                  onClick={handleGenerateToken}
                  disabled={loadingToken || !selectedAppId}
                  className="bg-[#1a2b49] text-white px-4 py-2 rounded-lg text-[10px] font-bold hover:bg-[#3ab1bb] disabled:bg-gray-300 transition-colors uppercase tracking-widest"
                >
                  {loadingToken ? "Génération..." : "Générer Access Token"}
                </button>
              </div>
            </div>

            {/* LISTE DES OPÉRATIONS DYNAMIQUES */}
            <div className="space-y-4">
              {details.operations?.map((op, idx) => {
                const opId = `op-${idx}`;
                const pathVars = op.target.match(/\{([^}]+)\}/g) || [];

                return (
                  <div key={opId} className="bg-white border rounded-xl overflow-hidden shadow-sm hover:border-[#3ab1bb]/30 transition-colors">
                    {/* Header Opération */}
                    <div className={`p-3 flex items-center gap-4 border-b ${op.verb === 'GET' ? 'bg-blue-50/40' : 'bg-green-50/40'}`}>
                      <span className={`px-3 py-1 rounded font-black text-[10px] text-white min-w-[50px] text-center ${op.verb === 'GET' ? 'bg-blue-500' : 'bg-green-500'}`}>
                        {op.verb}
                      </span>
                      <span className="font-mono text-xs font-bold text-slate-600">{op.target}</span>
                    </div>

                    <div className="p-5 space-y-4">
                      {/* Inputs pour Variables de chemin */}
                      {pathVars.length > 0 && (
                        <div className="space-y-3">
                          <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest">URL Path Variables</p>
                          {pathVars.map(v => {
                            const varName = v.replace(/[{}]/g, '');
                            return (
                              <div key={varName} className="flex items-center gap-4 max-w-md">
                                <label className="text-xs font-mono text-slate-500 w-32 shrink-0">{varName}*</label>
                                <input 
                                  type="text"
                                  className="flex-1 p-2 bg-slate-50 border-b outline-none focus:border-[#3ab1bb] text-sm"
                                  placeholder={`Entrer ${varName}`}
                                  onChange={(e) => setOpInputs(prev => ({
                                    ...prev,
                                    [opId]: { ...prev[opId], [varName]: e.target.value }
                                  }))}
                                />
                              </div>
                            );
                          })}
                        </div>
                      )}

                      <div className="flex justify-end pt-2">
                        <button 
                          onClick={() => executeOperationTest(op, idx)}
                          disabled={!accessToken || executingOp === opId}
                          className="bg-[#3ab1bb] text-white px-6 py-2 rounded-lg font-black text-[10px] hover:scale-105 active:scale-95 transition-all disabled:opacity-50 uppercase tracking-widest shadow-lg shadow-cyan-100"
                        >
                          {executingOp === opId ? "Appel Gateway..." : "Try it out"}
                        </button>
                      </div>

                      {/* Zone de Résultat JSON */}
                     {testResults[opId] && (
  <div className="mt-4 animate-in fade-in slide-in-from-top-2">
    <div className="flex justify-between items-center mb-2">
      <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Server Response</p>
      
      {/* Badge dynamique selon si c'est une erreur ou un succès */}
      <span className={`text-[9px] font-bold px-2 py-0.5 rounded border uppercase ${
        testResults[opId].error || testResults[opId].code ? 
        'text-red-600 bg-red-50 border-red-100' : 
        'text-green-600 bg-green-50 border-green-100'
      }`}>
        {testResults[opId].error || testResults[opId].code ? "Error Response" : "Status: 200 OK"}
      </span>
    </div>
    
    {/* Affichage du JSON (qu'il soit un succès ou une erreur) */}
    <pre className={`p-4 rounded-xl text-[10px] font-mono overflow-auto max-h-60 border shadow-inner custom-scrollbar ${
      testResults[opId].error || testResults[opId].code ? 
      'bg-red-900/10 text-red-700 border-red-200' : 
      'bg-[#1a2b49] text-emerald-400 border-slate-700'
    }`}>
      {JSON.stringify(testResults[opId], null, 2)}
    </pre>
  </div>
)}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* B. LISTE DES APPLICATIONS ABONNÉES (NOUVEAU) */}
          <section className="bg-white rounded-[2rem] p-8 border shadow-sm">
            <div className="flex items-center justify-between mb-8">
              <div>
                <h2 className="text-lg font-black text-[#1a2b49] uppercase tracking-tighter flex items-center gap-2">
                  <svg className="w-5 h-5 text-[#3ab1bb]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-7h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                    Abonnés
                </h2>
                <p className="text-[10px] font-medium text-slate-400 mt-1 uppercase tracking-widest">Liste des applications utilisant cette API</p>
              </div>
              <span className="bg-slate-50 text-slate-400 text-[10px] font-black px-4 py-1 rounded-full border border-slate-100">
                {details.subscriptions?.length || 0} APPS
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {details.subscriptions?.map((sub) => (
                <div 
                  key={sub.subscriptionId}
                  onClick={() => navigate(`/application/${sub.applicationInfo.applicationId}`)}
                  className="group flex items-center justify-between p-5 border-2 border-transparent bg-slate-50/50 rounded-2xl hover:border-[#3ab1bb] hover:bg-white cursor-pointer transition-all duration-300"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-white border rounded-xl flex items-center justify-center font-black text-[#1a2b49] shadow-sm group-hover:bg-[#3ab1bb] group-hover:text-white transition-all">
                      {sub.applicationInfo.name.charAt(0)}
                    </div>
                    <div>
                      <h3 className="font-bold text-[#1a2b49] group-hover:text-[#3ab1bb] transition-colors">
                        {sub.applicationInfo.name}
                      </h3>
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-0.5">
                        Subscription policy: <span className="text-[#3ab1bb]">{sub.throttlingPolicy}</span>
                      </p>
                    </div>
                  </div>
                  <div className="text-slate-300 group-hover:text-[#3ab1bb] group-hover:translate-x-1 transition-all">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                    </svg>
                  </div>
                </div>
              ))}

              {(!details.subscriptions || details.subscriptions.length === 0) && (
                <div className="col-span-full py-12 text-center border-2 border-dashed rounded-[2rem] text-slate-400">
                  <p className="text-sm font-medium italic">Aucun abonnement actif trouvé pour cette API.</p>
                </div>
              )}
            </div>
          </section>

        </div>
      </div>
    </div>
  );
};

export default ApiDetailsPage;