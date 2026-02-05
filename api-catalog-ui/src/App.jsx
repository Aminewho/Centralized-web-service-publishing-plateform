import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ApiListPage from './pages/ApiListPage';
import ApiDetailsPage from './pages/ApiDetailsPage';
import AppListPage from './pages/AppListPage';
import AppDetailsPage from './pages/AppDetailsPage';
function App() {
  return (
    <Router>
      <div className="min-h-screen bg-[#f8fafc]">
        {/* La Navbar reste visible sur toutes les pages */}
        <Navbar />
        
        {/* Conteneur principal avec padding pour le contenu */}
        <main className="max-w-7xl mx-auto p-6 md:p-10">
          <Routes>
            {/* Route pour la liste des APIs (Ta page de Cards style RNE) */}
            <Route path="/" element={<ApiListPage />} />
            
            {/* Route dynamique pour les détails d'une API spécifique */}
            <Route path="/api/:id" element={<ApiDetailsPage />} />
            
            {/* On pourra ajouter la route Applications ici plus tard */}
            <Route path="/applications" element={<AppListPage />} />
            <Route path="/application/:id" element={<AppDetailsPage />} />
          </Routes>
        </main>

        {/* Footer discret style institutionnel */}
        <footer className="border-t bg-white py-6 mt-12">
          <div className="max-w-7xl mx-auto px-6 text-center text-xs text-slate-400 uppercase tracking-widest">
            © 2026 API Catalog - Portail de Gouvernance WSO2
          </div>
        </footer>
      </div>
    </Router>
  );
}

export default App;