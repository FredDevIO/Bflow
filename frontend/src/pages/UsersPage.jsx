import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useTranslation } from '../context/LanguageContext';
import Swal from 'sweetalert2';

export default function UsersPage() {
    const { t } = useTranslation();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const response = await api.get('/users/all');

            // Handle both direct array and paginated results
            let data = [];
            if (response.data && response.data.content) {
                data = response.data.content;
            } else {
                data = Array.isArray(response.data) ? response.data : [];
            }

            setUsers(data);
            setError(null);
        } catch (err) {
            console.error(err);
            setError(t('users.errorFetch'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleDeleteUser = async (id, username) => {
        const result = await Swal.fire({
            title: t('users.delete'),
            text: t('users.deleteConfirm', [username]),
            icon: 'warning',
            showCancelButton: true,
            background: '#0f172a',
            color: '#f8fafc',
            confirmButtonColor: '#ef4444',
            cancelButtonColor: '#334155',
            confirmButtonText: t('common.delete'),
            cancelButtonText: t('common.cancel')
        });

        if (result.isConfirmed) {
            try {
                await api.delete(`/users/${id}`);
                Swal.fire({
                    title: t('common.deleted'),
                    text: t('users.deleteSuccess'),
                    icon: 'success',
                    background: '#0f172a',
                    color: '#f8fafc',
                    timer: 1500,
                    showConfirmButton: false
                });
                fetchUsers();
            } catch (err) {
                Swal.fire({
                    title: t('common.error'),
                    text: err.response?.data?.message || t('users.errorDelete'),
                    icon: 'error',
                    background: '#0f172a',
                    color: '#f8fafc'
                });
            }
        }
    };

    const handleUpdatePassword = async (id, username) => {
        const { value: newPassword } = await Swal.fire({
            title: t('users.changePassword'),
            text: t('users.updatePassword', [username]),
            input: 'password',
            inputPlaceholder: t('users.passwordPlaceholder'),
            showCancelButton: true,
            background: '#0f172a',
            color: '#f8fafc',
            confirmButtonColor: '#10b981',
            cancelButtonColor: '#334155',
            inputAttributes: {
                autocapitalize: 'off',
                autocorrect: 'off'
            },
            inputValidator: (value) => {
                if (!value) {
                    return t('users.passwordEmpty');
                }
                if (value.length < 8) {
                    return t('users.passwordShort');
                }
            }
        });

        if (newPassword) {
            try {
                await api.post(`/users/${id}/password`, { newPassword });
                Swal.fire({
                    title: t('common.success'),
                    text: t('users.passwordSuccess'),
                    icon: 'success',
                    background: '#0f172a',
                    color: '#f8fafc',
                    timer: 1500,
                    showConfirmButton: false
                });
            } catch (err) {
                Swal.fire({
                    title: t('common.error'),
                    text: err.response?.data?.message || t('users.errorPassword'),
                    icon: 'error',
                    background: '#0f172a',
                    color: '#f8fafc'
                });
            }
        }
    };

    const getRoleBadge = (role) => {
        if (role === 'ADMIN') {
            return <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-semibold tracking-wide">{t('users.admin')}</span>;
        }
        return <span className="px-2.5 py-0.5 rounded-full bg-slate-500/10 text-slate-400 border border-slate-500/20 text-xs font-semibold tracking-wide">{t('users.user')}</span>;
    };

    return (
        <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-white mb-2">{t('users.title')}</h1>
                    <p className="text-slate-400">{t('users.subtitle')}</p>
                </div>
                <button
                    onClick={fetchUsers}
                    className="flex items-center gap-2 px-4 py-2 bg-white/5 hover:bg-white/10 text-white rounded-xl border border-white/10 transition-colors"
                >
                    <svg className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
                    {t('users.refresh')}
                </button>
            </div>

            {error && (
                <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 flex items-center gap-3">
                    <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                    <p className="font-medium text-sm">{error}</p>
                </div>
            )}

            <div className="bg-white/[0.02] border border-white/5 rounded-3xl overflow-hidden shadow-2xl backdrop-blur-md">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm text-slate-300">
                        <thead className="bg-slate-900/50 text-slate-400 font-medium border-b border-white/5">
                            <tr>
                                <th className="px-6 py-4">{t('users.id')}</th>
                                <th className="px-6 py-4">{t('users.username')}</th>
                                <th className="px-6 py-4 text-center">{t('categories.typeLabel')}</th>
                                <th className="px-6 py-4 text-center">{t('users.actions')}</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-white/5">
                            {loading && users.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="px-6 py-12 text-center text-slate-500">
                                        <div className="flex flex-col items-center justify-center gap-3">
                                            <div className="w-8 h-8 flex items-center justify-center">
                                                <svg className="animate-spin h-6 w-6 text-emerald-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                                            </div>
                                            <span className="text-sm font-medium">{t('common.loading')}</span>
                                        </div>
                                    </td>
                                </tr>
                            ) : users.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="px-6 py-12 text-center text-slate-500 italic">
                                        <div className="flex flex-col items-center justify-center gap-2">
                                            <svg className="w-8 h-8 text-slate-700" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" /></svg>
                                            <p className="font-medium">{t('users.noUsers')}</p>
                                        </div>
                                    </td>
                                </tr>
                            ) : (
                                users.map((u) => (
                                    <tr key={u.id || u.username} className="hover:bg-white/[0.02] transition-colors group">
                                        <td className="px-6 py-4 font-mono text-xs text-slate-500">
                                            #{u.id}
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-emerald-500/20 to-teal-400/20 flex items-center justify-center text-emerald-400 font-bold border border-emerald-500/20 group-hover:border-emerald-500/50 transition-colors shrink-0">
                                                    {u.username.charAt(0).toUpperCase()}
                                                </div>
                                                <div className="flex flex-col min-w-0">
                                                    <span className="font-semibold text-white truncate">{u.username}</span>
                                                    <span className="text-xs text-slate-500 truncate">{u.email}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-center">
                                            {getRoleBadge(u.role)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center justify-center gap-2">
                                                <button
                                                    onClick={() => handleUpdatePassword(u.id, u.username)}
                                                    className="flex items-center gap-2 px-3 py-1.5 text-xs font-semibold text-slate-400 hover:text-amber-400 hover:bg-amber-400/10 rounded-lg transition-all"
                                                    title={t('users.changePassword')}
                                                >
                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" /></svg>
                                                    {t('users.changePassword')}
                                                </button>
                                                <button
                                                    onClick={() => handleDeleteUser(u.id, u.username)}
                                                    className="flex items-center gap-2 px-3 py-1.5 text-xs font-semibold text-slate-400 hover:text-red-400 hover:bg-red-400/10 rounded-lg transition-all"
                                                    title={t('users.delete')}
                                                >
                                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                                    {t('common.delete')}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
