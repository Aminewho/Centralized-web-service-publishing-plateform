import React, { useEffect, useState } from 'react';
import axios from 'axios';
import AppCard from '../components/AppCard';

const AppListPage = () => {
  const [apps, setApps] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/applications')
      .then(res => setApps(res.data.applications))
      .catch(err => console.error(err));
  }, []);

  return (
    <div className="animate-in fade-in duration-700">
      <div className="text-center mb-12">
        <h1 className="text-3xl font-black text-[#1a2b49] mb-2 uppercase tracking-tighter">Mes Applications</h1>
        <div className="w-20 h-1.5 bg-[#3ab1bb] mx-auto rounded-full"></div>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {apps.map(app => (
          <AppCard key={app.applicationId} app={app} />
        ))}
      </div>
    </div>
  );
};

export default AppListPage;