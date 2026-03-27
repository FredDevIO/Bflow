import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import api from '../api/axios';

export default function SettingsPage() {
    const { user, setUser } = useAuth();
    const { t, changeLanguage } = useTranslation();

    const [toast, setToast] = useState({ message: '', type: 'success' });

    // Profile State
    const [profile, setProfile] = useState({
        username: user?.username || '',
        email: user?.email || '',
        currency: user?.currency || 'USD',
        language: (user?.language || 'ENGLISH').toUpperCase()
    });
    const [isProfileLoading, setProfileLoading] = useState(false);

    // Password State
    const [passwords, setPasswords] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [isPasswordLoading, setPasswordLoading] = useState(false);

    const showToast = (message, type) => {
        setToast({ message, type });
        setTimeout(() => setToast({ message: '', type: 'success' }), 3000);
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setProfileLoading(true);
        try {
            const response = await api.post('/users/me/profile', {
                newUsername: profile.username,
                newEmail: profile.email,
                currency: profile.currency,
                language: profile.language
            });
            
            // Update Auth Context
            const updatedUser = { ...user, ...response.data };
            setUser(updatedUser);
            
            // Update UI Language immediately
            if (profile.language) {
                changeLanguage(profile.language);
            }
            
            showToast(t('settings.updateSuccess'), 'success');
        } catch (error) {
            const errorMsg = error.response?.data?.message || error.response?.data?.error || t('settings.updateError');
            showToast(errorMsg, 'error');
        } finally {
            setProfileLoading(false);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        if (passwords.newPassword.length < 8) {
            return showToast(t('settings.passwordShort'), 'error');
        }
        if (passwords.newPassword !== passwords.confirmPassword) {
            return showToast(t('settings.passwordMismatch'), 'error');
        }
        setPasswordLoading(true);
        try {
            await api.post('/users/me/password', {
                oldPassword: passwords.oldPassword,
                newPassword: passwords.newPassword
            });
            setPasswords({ oldPassword: '', newPassword: '', confirmPassword: '' });
            showToast(t('settings.passwordSuccess'), 'success');
        } catch (error) {
            const errorMsg = error.response?.data?.message || error.response?.data?.error || t('settings.passwordError');
            showToast(errorMsg, 'error');
        } finally {
            setPasswordLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto space-y-8 relative">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-white mb-2">{t('settings.title')}</h1>
                <p className="text-slate-400">{t('settings.subtitle')}</p>
            </div>

            {toast.message && (
                <div className={`p-4 rounded-xl border flex items-center gap-3 animate-in fade-in slide-in-from-top-2 z-50 ${toast.type === 'success' ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' : 'bg-red-500/10 border-red-500/20 text-red-400'}`}>
                    <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        {toast.type === 'success'
                            ? <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            : <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        }
                    </svg>
                    <p className="font-medium text-sm">{toast.message}</p>
                </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Profile Form */}
                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 relative overflow-hidden backdrop-blur-sm">
                    <h2 className="text-xl font-semibold text-white mb-6">{t('settings.personalInfo')}</h2>
                    <form onSubmit={handleProfileSubmit} className="space-y-5">
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.username')}</label>
                            <input
                                type="text"
                                className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                                value={profile.username}
                                onChange={(e) => setProfile({ ...profile, username: e.target.value })}
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.email')}</label>
                            <input
                                type="email"
                                className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                                value={profile.email}
                                onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                                required
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.currency')}</label>
                                <div className="relative">
                                    <select
                                        className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all appearance-none pr-10"
                                        value={profile.currency}
                                        onChange={(e) => setProfile({ ...profile, currency: e.target.value })}
                                    >
                                        <option value="USD" className="bg-slate-900">USD ($)</option>
                                        <option value="EUR" className="bg-slate-900">EUR (€)</option>
                                        <option value="GBP" className="bg-slate-900">GBP (£)</option>
                                    </select>
                                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/></svg>
                                    </div>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.language')}</label>
                                <div className="relative">
                                    <select
                                        className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all appearance-none pr-10"
                                        value={profile.language}
                                        onChange={(e) => setProfile({ ...profile, language: e.target.value })}
                                    >
                                        <option value="ENGLISH" className="bg-slate-900">{t('lang.english')}</option>
                                        <option value="SPANISH" className="bg-slate-900">{t('lang.spanish')}</option>
                                        <option value="FRENCH" className="bg-slate-900">{t('lang.french')}</option>
                                        <option value="GERMAN" className="bg-slate-900">{t('lang.german')}</option>
                                        <option value="ITALIAN" className="bg-slate-900">{t('lang.italian')}</option>
                                        <option value="PORTUGUESE" className="bg-slate-900">{t('lang.portuguese')}</option>
                                    </select>
                                    <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/></svg>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <button
                            type="submit"
                            disabled={isProfileLoading}
                            className="w-full py-3.5 px-4 bg-white/5 hover:bg-emerald-500/10 hover:text-emerald-400 hover:border-emerald-500/30 text-white font-semibold rounded-xl border border-white/10 transition-all disabled:opacity-50 mt-4 active:scale-[0.98]"
                        >
                            {isProfileLoading ? t('common.loading') : t('common.update')}
                        </button>
                    </form>
                </div>

                {/* Password Form */}
                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 relative overflow-hidden backdrop-blur-sm">
                    <h2 className="text-xl font-semibold text-white mb-6">{t('settings.security')}</h2>
                    <form onSubmit={handlePasswordSubmit} className="space-y-5">
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.currentPassword')}</label>
                            <input
                                type="password"
                                className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                                value={passwords.oldPassword}
                                onChange={(e) => setPasswords({ ...passwords, oldPassword: e.target.value })}
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.newPassword')}</label>
                            <input
                                type="password"
                                className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                                value={passwords.newPassword}
                                onChange={(e) => setPasswords({ ...passwords, newPassword: e.target.value })}
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 uppercase tracking-widest">{t('settings.confirmPassword')}</label>
                            <input
                                type="password"
                                className="w-full p-3.5 bg-slate-950/50 border border-slate-800 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500 transition-all"
                                value={passwords.confirmPassword}
                                onChange={(e) => setPasswords({ ...passwords, confirmPassword: e.target.value })}
                                required
                            />
                        </div>
                        <button
                            type="submit"
                            disabled={isPasswordLoading}
                            className="w-full py-3.5 px-4 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-semibold rounded-xl transition-all disabled:opacity-50 mt-4 shadow-[0_0_15px_rgba(16,185,129,0.1)] active:scale-[0.98]"
                        >
                            {isPasswordLoading ? t('common.loading') : t('settings.updatePassword')}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}