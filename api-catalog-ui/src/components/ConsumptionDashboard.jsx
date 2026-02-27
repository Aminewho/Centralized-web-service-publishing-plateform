import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
// Icons come from here
import { ChevronDown } from 'lucide-react'; 
// Chart components come from here
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer 
} from 'recharts';
// Pie components (if you use them) come from here
import { PieChart, Pie, Cell, Legend } from 'recharts';
const API_BASE = "http://localhost:8080/api/history/search";

const ConsumptionDashboard = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({
    appName: '',
    apiName: '',
    start: '',
    end: ''
  });

  // Keep track of all available options regardless of current filter
  const [availableOptions, setAvailableOptions] = useState({ apps: [], apis: [] });

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (filters.appName) params.append('appName', filters.appName);
      if (filters.apiName) params.append('apiName', filters.apiName);
      if (filters.start) params.append('start', filters.start);
      if (filters.end) params.append('end', filters.end);

      const response = await axios.get(`${API_BASE}?${params.toString()}`);
      setLogs(response.data);

      // Populate dropdown options only on the first load or if they are empty
      if (availableOptions.apps.length === 0) {
        const apps = [...new Set(response.data.map(l => l.applicationName))].sort();
        const apis = [...new Set(response.data.map(l => l.apiName))].sort();
        setAvailableOptions({ apps, apis });
      }
    } catch (err) {
      console.error("Erreur lors de la récupération des logs", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  // --- DATA PREPARATION (useMemo for performance) ---
  const dailyData = useMemo(() => {
    const grouped = logs.reduce((acc, log) => {
      const date = new Date(log.requestDate).toLocaleDateString('fr-FR', {
        day: '2-digit', month: 'short'
      });
      const existing = acc.find(item => item.date === date);
      if (existing) existing.count += 1;
      else acc.push({ date, count: 1 });
      return acc;
    }, []);
    return grouped.sort((a, b) => new Date(a.date) - new Date(b.date));
  }, [logs]);

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8 animate-in fade-in duration-700">
      
      {/* Header */}
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-3xl font-black text-[#1a2b49] uppercase tracking-tighter">
            Analytics <span className="text-[#3ab1bb]">Consommation</span>
          </h1>
          <p className="text-slate-400 font-medium">Visualisez l'activité de vos passerelles APK en temps réel.</p>
        </div>
        {loading && <div className="text-[#3ab1bb] animate-pulse font-bold text-xs uppercase">Chargement...</div>}
      </div>

      {/* --- UPDATED FILTER BAR --- */}
      <div className="bg-white p-6 rounded-[2rem] shadow-sm border border-slate-100 flex flex-wrap gap-4 items-end">
        
        {/* Application Select */}
        <div className="flex-1 min-w-[180px] space-y-2">
          <label className="text-[10px] font-black text-slate-400 uppercase ml-2">Application</label>
          <div className="relative">
            <select 
              className="w-full p-3 bg-slate-50 rounded-xl outline-none focus:ring-2 ring-[#3ab1bb]/20 border-transparent text-sm font-bold appearance-none cursor-pointer"
              value={filters.appName}
              onChange={(e) => setFilters({...filters, appName: e.target.value})}
            >
              <option value="">Toutes les Apps</option>
              {availableOptions.apps.map(app => (
                <option key={app} value={app}>{app}</option>
              ))}
            </select>
            <div className="absolute inset-y-0 right-3 flex items-center pointer-events-none text-slate-400">
              <span className="text-[10px]">▼</span>
            </div>
          </div>
        </div>

        {/* API Select */}
        <div className="flex-1 min-w-[180px] space-y-2">
          <label className="text-[10px] font-black text-slate-400 uppercase ml-2">API</label>
          <div className="relative">
            <select 
              className="w-full p-3 bg-slate-50 rounded-xl outline-none focus:ring-2 ring-[#3ab1bb]/20 border-transparent text-sm font-bold appearance-none cursor-pointer"
              value={filters.apiName}
              onChange={(e) => setFilters({...filters, apiName: e.target.value})}
            >
              <option value="">Toutes les APIs</option>
              {availableOptions.apis.map(api => (
                <option key={api} value={api}>{api}</option>
              ))}
            </select>
            <div className="absolute inset-y-0 right-3 flex items-center pointer-events-none text-slate-400">
              <span className="text-[10px]">▼</span>
            </div>
          </div>
        </div>

        {/* Date Filters */}
        <div className="flex-1 min-w-[180px] space-y-2">
          <label className="text-[10px] font-black text-slate-400 uppercase ml-2">Début</label>
          <input 
            type="datetime-local" 
            className="w-full p-3 bg-slate-50 rounded-xl outline-none text-sm font-medium border-transparent focus:ring-2 ring-[#3ab1bb]/20"
            onChange={(e) => setFilters({...filters, start: e.target.value})}
          />
        </div>

        <div className="flex-1 min-w-[180px] space-y-2">
          <label className="text-[10px] font-black text-slate-400 uppercase ml-2">Fin</label>
          <input 
            type="datetime-local" 
            className="w-full p-3 bg-slate-50 rounded-xl outline-none text-sm font-medium border-transparent focus:ring-2 ring-[#3ab1bb]/20"
            onChange={(e) => setFilters({...filters, end: e.target.value})}
          />
        </div>

        <button 
          onClick={fetchLogs}
          disabled={loading}
          className="bg-[#1a2b49] text-white px-8 py-3 rounded-xl font-black uppercase tracking-widest hover:bg-[#3ab1bb] transition-all disabled:opacity-50 active:scale-95"
        >
          Filtrer
        </button>
      </div>

      {/* --- KPI CARDS --- */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-[2rem] border shadow-sm group hover:border-[#3ab1bb] transition-colors">
          <p className="text-[10px] font-black text-slate-400 uppercase">Total Requêtes</p>
          <p className="text-4xl font-black text-[#1a2b49] group-hover:text-[#3ab1bb] transition-colors">{logs.length}</p>
        </div>
        <div className="bg-[#3ab1bb] p-6 rounded-[2rem] shadow-lg shadow-cyan-100 text-white">
          <p className="text-[10px] font-black opacity-80 uppercase">Taux de Succès</p>
          <p className="text-4xl font-black">
            {logs.length > 0 ? ((logs.filter(l => l.status.startsWith('2')).length / logs.length) * 100).toFixed(1) : 0}%
          </p>
        </div>
        <div className="bg-[#1a2b49] p-6 rounded-[2rem] text-white">
          <p className="text-[10px] font-black opacity-80 uppercase">Erreurs Client (4xx)</p>
          <p className="text-4xl font-black text-red-400">
            {logs.filter(l => l.status.startsWith('4')).length}
          </p>
        </div>
      </div>

      {/* --- CHART --- */}
      <div className="bg-white p-8 rounded-[2rem] border shadow-sm h-[400px]">
        <h3 className="text-sm font-black text-[#1a2b49] uppercase mb-6 flex items-center gap-2">
          <div className="w-2 h-2 bg-[#3ab1bb] rounded-full" />
          Volume de requêtes par jour
        </h3>
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={dailyData}>
            <defs>
              <linearGradient id="colorCount" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3ab1bb" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#3ab1bb" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis dataKey="date" axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: 'bold', fill: '#94a3b8'}} dy={10} />
            <YAxis axisLine={false} tickLine={false} tick={{fontSize: 10, fontWeight: 'bold', fill: '#94a3b8'}} />
            <Tooltip 
              contentStyle={{ borderRadius: '15px', border: 'none', boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)', padding: '12px'}}
              labelStyle={{ fontWeight: 'black', color: '#1a2b49', marginBottom: '4px' }}
            />
            <Area type="monotone" dataKey="count" name="Requêtes" stroke="#3ab1bb" strokeWidth={3} fillOpacity={1} fill="url(#colorCount)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* --- LOGS TABLE --- */}
      <div className="bg-white rounded-[2rem] border overflow-hidden shadow-sm">
        <table className="w-full text-left border-collapse">
          <thead className="bg-slate-50 border-b border-slate-100">
            <tr>
              <th className="p-4 text-[10px] font-black text-slate-400 uppercase">Date</th>
              <th className="p-4 text-[10px] font-black text-slate-400 uppercase">App</th>
              <th className="p-4 text-[10px] font-black text-slate-400 uppercase">API</th>
              <th className="p-4 text-[10px] font-black text-slate-400 uppercase text-center">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-50">
            {logs.map((log) => (
              <tr key={log.id} className="hover:bg-slate-50/50 transition-colors">
                <td className="p-4 text-xs font-medium text-slate-500">{new Date(log.requestDate).toLocaleString()}</td>
                <td className="p-4 font-bold text-[#1a2b49] text-sm">{log.applicationName}</td>
                <td className="p-4 text-xs font-mono text-[#3ab1bb]">{log.apiName}</td>
                <td className="p-4 text-center">
                  <span className={`px-3 py-1 rounded-full text-[10px] font-black ${
                    log.status.startsWith('2') ? 'bg-green-50 text-green-600' : 'bg-red-50 text-red-600'
                  }`}>
                    {log.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {logs.length === 0 && !loading && (
          <div className="p-20 text-center text-slate-300 font-bold uppercase tracking-widest">
            Aucun log trouvé
          </div>
        )}
      </div>
    </div>
  );
};

export default ConsumptionDashboard;