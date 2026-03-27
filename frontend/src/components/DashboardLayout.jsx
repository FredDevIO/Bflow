import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import logoUrl from '../assets/bflow-logo-wb.png';

export default function DashboardLayout() {
    const { user, logout } = useAuth();
    const { t } = useTranslation();
    const location = useLocation();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    // Close sidebar on route change on mobile
    useEffect(() => {
        setIsSidebarOpen(false);
    }, [location.pathname]);

    const navItems = [
        { path: '/dashboard', label: t('nav.overview'), icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
        )},
        { path: '/transactions', label: t('nav.transactions'), icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
        )},
        { path: '/analytics', label: t('nav.analytics'), icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
        )},
    ];

    if (user?.role === 'ADMIN') {
        navItems.push({ path: '/users', label: t('nav.users'), icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
        )});
    }

    navItems.push({ path: '/settings', label: t('nav.settings'), icon: (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
    )});

    return (
        <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden selection:bg-emerald-500/30">
            {/* Sidebar */}
            <aside className={`fixed inset-y-0 left-0 z-50 w-64 bg-slate-900/80 backdrop-blur-xl border-r border-slate-800 transform transition-transform duration-300 ease-in-out ${isSidebarOpen ? 'translate-x-0' : '-translate-x-full'} lg:relative lg:translate-x-0 flex flex-col`}>
                <div className="flex items-center gap-3 p-6 h-20 border-b border-slate-800">
                    <img src={logoUrl} alt="Bflow Logo" className="w-8 h-8 drop-shadow-[0_0_8px_rgba(16,185,129,0.5)]" />
                    <span className="text-2xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">Bflow</span>
                </div>

                <nav className="flex-1 overflow-y-auto py-6 px-4 space-y-1.5 hide-scrollbar">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) =>
                                `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 group relative overflow-hidden ${
                                    isActive 
                                        ? 'text-white bg-emerald-500/10 border border-emerald-500/20 shadow-[0_0_15px_rgba(16,185,129,0.1)]' 
                                        : 'text-slate-400 hover:text-white hover:bg-slate-800'
                                }`
                            }
                        >
                            {({ isActive }) => (
                                <>
                                    {isActive && <div className="absolute left-0 w-1 h-8 bg-emerald-500 rounded-r-full shadow-[0_0_10px_rgba(16,185,129,0.8)]" />}
                                    <div className={`transition-colors duration-200 ${isActive ? 'text-emerald-400' : 'group-hover:text-slate-300'}`}>
                                        {item.icon}
                                    </div>
                                    <span className="font-medium tracking-wide">{item.label}</span>
                                </>
                            )}
                        </NavLink>
                    ))}
                </nav>

                <div className="p-4 border-t border-slate-800">
                    <button 
                        onClick={logout}
                        className="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-slate-400 hover:text-white hover:bg-red-500/10 hover:border-red-500/20 border border-transparent transition-all duration-200 group"
                    >
                        <svg className="w-5 h-5 group-hover:text-red-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                        <span className="font-medium">{t('nav.logout')}</span>
                    </button>
                    
                    <div className="mt-4 px-4 pb-2 flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center text-white font-bold shadow-[0_0_10px_rgba(16,185,129,0.3)]">
                                {user?.username?.charAt(0).toUpperCase()}
                            </div>
                            <div className="flex flex-col">
                                <span className="text-sm font-semibold text-white truncate max-w-[100px]">{user?.username}</span>
                                <span className="text-xs text-slate-500 truncate max-w-[100px]">{user?.role}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </aside>

            {/* Mobile Overlay */}
            {isSidebarOpen && (
                <div 
                    className="fixed inset-0 z-40 bg-slate-950/80 backdrop-blur-sm lg:hidden transition-opacity"
                    onClick={() => setIsSidebarOpen(false)}
                />
            )}

            {/* Main Content */}
            <main className="flex-1 flex flex-col h-screen overflow-hidden relative">
                <header className="h-20 flex-shrink-0 flex items-center justify-between px-6 lg:px-10 bg-slate-900/40 backdrop-blur-md border-b border-white/5 z-30 sticky top-0">
                    <div className="flex items-center gap-4">
                        <button 
                            onClick={() => setIsSidebarOpen(true)}
                            className="lg:hidden p-2 -ml-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
                        >
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16"/></svg>
                        </button>
                        <h1 className="text-xl font-semibold text-white truncate max-w-[200px] sm:max-w-none">
                            {t('dashboard.welcome', [user?.username])}
                        </h1>
                    </div>
                </header>

                <div id="main-content-scroll" className="flex-1 overflow-y-auto overflow-x-hidden p-6 lg:p-10 relative">
                    <div className="absolute top-[-20%] right-[-10%] w-[500px] h-[500px] bg-emerald-600/5 rounded-full blur-[120px] pointer-events-none" />
                    <Outlet />
                </div>
            </main>
        </div>
    );
}