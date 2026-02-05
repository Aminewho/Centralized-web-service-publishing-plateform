import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Navbar = () => {
  const location = useLocation();
  
  const tabs = [
    { id: 'apis', label: 'GESTION DES APIS', path: '/' },
    { id: 'apps', label: 'APPLICATIONS & DEVS', path: '/applications' },
    { id: 'admin', label: 'POLITIQUES & ADMIN', path: '/admin' }
  ];

 const isActive = (path) => {
  // Si on est sur l'onglet API (path === '/')
  if (path === '/') {
    // On l'allume si on est à la racine OU si l'URL commence par /api
    return location.pathname === '/' || location.pathname.startsWith('/api') 
  }
  else if (path === '/applications') {
    // Pour l'onglet Applications, on l'allume si l'URL commence par /applications
    return location.pathname.startsWith('/applications') || location.pathname.startsWith('/application');
  }
  
  // Pour les autres onglets (Applications, Admin)
  // On vérifie simplement si l'URL commence par le chemin de l'onglet
  return location.pathname.startsWith(path);
};

  return (
    <nav className="bg-[#1a2b49] text-white shadow-xl sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex justify-between items-center h-20">
          
          <div className="flex items-center space-x-10">
            {/* Logo cliquable pour revenir à l'accueil */}
            <Link to="/" className="flex flex-col leading-none border-r border-slate-700 pr-8">
              <span className="text-2xl font-black tracking-tighter text-white">
                API<span className="text-[#3ab1bb]">CATALOG</span>
              </span>
              <span className="text-[10px] uppercase tracking-[0.2em] text-slate-400 mt-1">
                Gouvernance Services
              </span>
            </Link>

            {/* Menu de Navigation avec Link */}
            <div className="hidden md:flex space-x-8">
              {tabs.map((tab) => (
                <Link
                  key={tab.id}
                  to={tab.path}
                  className={`text-sm font-bold transition-all pb-2 border-b-2 ${
                    isActive(tab.path) 
                    ? 'border-[#3ab1bb] text-white' 
                    : 'border-transparent text-slate-400 hover:text-white'
                  }`}
                >
                  {tab.label}
                </Link>
              ))}
            </div>
          </div>

          {/* Profil */}
          <div className="flex items-center space-x-4">
            <div className="text-right mr-2">
              <p className="text-xs font-bold text-[#3ab1bb]">ADMINISTRATEUR</p>
              <p className="text-[11px] text-slate-300">a.ziadi@portail.tn</p>
            </div>
            <div className="h-10 w-10 bg-slate-700 border border-slate-600 rounded-lg flex items-center justify-center font-bold text-[#3ab1bb]">
              AZ
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;