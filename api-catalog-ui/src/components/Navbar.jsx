import React from 'react';

const Navbar = ({ activeTab, setActiveTab }) => {
  const tabs = [
    { id: 'apis', label: 'GESTION DES APIS' },
    { id: 'apps', label: 'APPLICATIONS & DEVS' },
    { id: 'admin', label: 'POLITIQUES & ADMIN' }
  ];

  return (
    <nav className="bg-[#1a2b49] text-white shadow-xl">
      <div className="max-w-7xl mx-auto px-6">
        <div className="flex justify-between items-center h-20">
          
          <div className="flex items-center space-x-10">
            <div className="flex flex-col leading-none border-r border-slate-700 pr-8">
              <span className="text-2xl font-black tracking-tighter text-white">
                API<span className="text-[#3ab1bb]">CATALOG</span>
              </span>
              <span className="text-[10px] uppercase tracking-[0.2em] text-slate-400 mt-1">
                Gouvernance Services
              </span>
            </div>

            <div className="hidden md:flex space-x-8">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`text-sm font-bold transition-all pb-2 border-b-2 ${
                    activeTab === tab.id 
                    ? 'border-[#3ab1bb] text-white' 
                    : 'border-transparent text-slate-400 hover:text-white'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </div>

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