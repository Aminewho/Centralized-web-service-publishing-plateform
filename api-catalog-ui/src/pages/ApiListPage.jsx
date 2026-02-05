import React, { useEffect, useState } from 'react';
import axios from 'axios';
import ApiCard from '../components/ApiCard';
import { useParams } from 'react-router-dom';
const ApiListPage = () => {
  const [apis, setApis] = useState([]);
const { id } = useParams(); // Récupère l'ID depuis l'URL
  useEffect(() => {
    axios.get('http://localhost:8080/apis')
      .then(res => setApis(res.data.apis))
      .catch(err => console.error("Erreur Backend:", err));
  }, []);

  return (
    <div className="w-full bg-[#f8fafc] py-12"> 
      <div className="max-w-7xl mx-auto px-4">
        <h1 className="text-center text-3xl font-bold text-[#1a2b49] mb-12 uppercase">
          Accès rapide aux services
        </h1>
        
        {/* Grille forcée : 4 colonnes sur PC, 1 sur Mobile */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {apis.map(api => (
            <ApiCard key={api.id} api={api} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default ApiListPage;