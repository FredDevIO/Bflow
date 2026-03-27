import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import TransactionsPage from './pages/TransactionsPage';
import AnalyticsPage from './pages/AnalyticsPage';
import SettingsPage from './pages/SettingsPage';
import UsersPage from './pages/UsersPage';
import DashboardLayout from './components/DashboardLayout';
import { useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';
import ScrollToTop from './components/ScrollToTop';

function App() {
  const { user, loading } = useAuth();
  const theme = user?.theme?.toLowerCase() || 'dark';

  useEffect(() => {
    if (theme === 'light') {
      document.documentElement.classList.remove('dark');
      document.body.style.backgroundColor = '#f8fafc';
    } else {
      document.documentElement.classList.add('dark');
      document.body.style.backgroundColor = '#020617';
    }
  }, [theme]);

  if (loading) {
    return (
      <div className={`min-h-screen flex items-center justify-center ${theme === 'light' ? 'bg-slate-50' : 'bg-slate-950'}`}>
        <div className="w-8 h-8 border-4 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${theme === 'light' ? 'bg-slate-50 text-slate-950' : 'bg-slate-950 text-slate-100'}`}>
      <LanguageProvider>
        <BrowserRouter>
          <ScrollToTop />
          <Routes>
            {!user ? (
              <>
                <Route path="/login" element={<LoginPage />} />
                <Route path="*" element={<Navigate to="/login" replace />} />
              </>
            ) : (
              <Route element={<DashboardLayout />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/transactions" element={<TransactionsPage />} />
                <Route path="/analytics" element={<AnalyticsPage />} />
                {user?.role === 'ADMIN' ? (
                  <Route path="/users" element={<UsersPage />} />
                ) : (
                  <Route path="/users" element={<Navigate to="/dashboard" replace />} />
                )}
                <Route path="/settings" element={<SettingsPage />} />
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
              </Route>
            )}
          </Routes>
        </BrowserRouter>
      </LanguageProvider>
    </div>
  );
}

export default App;