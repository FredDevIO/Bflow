import { useState, useEffect } from 'react';
import api from '../api/axios';
import { useTranslation } from '../context/LanguageContext';
import Swal from 'sweetalert2';

export default function CategoryManager() {
    const { t } = useTranslation();
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);

    // Form State
    const [name, setName] = useState('');
    const [type, setType] = useState('EXPENSE');
    const [color, setColor] = useState('#10b981'); // Default emerald
    const [editingId, setEditingId] = useState(null);

    const fetchCategories = async () => {
        setLoading(true);
        try {
            const res = await api.get('/categories');
            setCategories(res.data || []);
        } catch (error) {
            console.error('Error fetching categories:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const resetForm = () => {
        setName('');
        setType('EXPENSE');
        setColor('#10b981');
        setEditingId(null);
    };

    const handleEditStart = (cat) => {
        setEditingId(cat.id);
        setName(cat.name);
        setType(cat.categoryType || cat.type || 'EXPENSE');
        setColor(cat.color || '#10b981');
        document.getElementById('category-form-top')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!name.trim()) return;

        const payload = {
            name,
            type,
            color
        };

        try {
            if (editingId) {
                await api.put(`/categories/${editingId}`, payload);
            } else {
                await api.post('/categories', payload);
            }

            resetForm();
            fetchCategories();

            Swal.fire({
                title: t('common.success'),
                text: editingId ? t('categories.updated') : t('categories.saved'),
                icon: 'success',
                background: '#0f172a',
                color: '#f8fafc',
                confirmButtonColor: '#10b981',
                timer: 2000,
                showConfirmButton: false
            });
        } catch (error) {
            Swal.fire({
                title: t('common.error'),
                text: error.response?.data?.message || t('categories.errorSave'),
                icon: 'error',
                background: '#0f172a',
                color: '#f8fafc',
                confirmButtonColor: '#ef4444'
            });
        }
    };

    const handleDeleteCategory = async (id) => {
        const result = await Swal.fire({
            title: t('categories.confirmDeleteTitle'),
            text: t('categories.confirmDelete'),
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
                await api.delete(`/categories/${id}`);
                fetchCategories();
                Swal.fire({
                    title: t('common.deleted'),
                    text: t('categories.deletedMsg'),
                    icon: 'success',
                    background: '#0f172a',
                    color: '#f8fafc',
                    confirmButtonColor: '#10b981',
                    timer: 2000,
                    showConfirmButton: false
                });
            } catch (error) {
                Swal.fire({
                    title: t('common.error'),
                    text: error.response?.data?.message || t('categories.errorDelete'),
                    icon: 'error',
                    background: '#0f172a',
                    color: '#f8fafc',
                    confirmButtonColor: '#ef4444'
                });
            }
        }
    };

    const colors = [
        { hex: '#10b981', name: 'Emerald' },
        { hex: '#3b82f6', name: 'Blue' },
        { hex: '#ef4444', name: 'Red' },
        { hex: '#f59e0b', name: 'Amber' },
        { hex: '#8b5cf6', name: 'Violet' },
        { hex: '#ec4899', name: 'Pink' },
        { hex: '#6366f1', name: 'Indigo' },
        { hex: '#94a3b8', name: 'Slate' }
    ];

    return (
        <div className="space-y-8 animate-in fade-in zoom-in-95 duration-300">
            <div id="category-form-top" className={`bg-white/[0.02] border rounded-3xl p-6 shadow-2xl backdrop-blur-md transition-all duration-300 ${editingId ? 'border-amber-500/50' : 'border-white/5'}`}>
                <div className="flex items-center justify-between mb-6">
                    <h2 className="text-xl font-semibold text-white">{editingId ? t('categories.edit') : t('categories.new')}</h2>
                    {editingId && (
                        <button onClick={resetForm} className="text-amber-500 hover:text-amber-400 text-xs font-semibold underline">{t('common.cancel')}</button>
                    )}
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('categories.nameLabel')}</label>
                            <input
                                type="text"
                                placeholder={t('categories.namePlaceholder')}
                                className="w-full bg-slate-950/50 border border-slate-800 text-white rounded-xl px-4 py-3 outline-none focus:border-emerald-500 transition-colors"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('categories.typeLabel')}</label>
                            <div className="flex bg-slate-950/50 rounded-xl p-1 border border-slate-800">
                                <button
                                    type="button"
                                    onClick={() => setType('EXPENSE')}
                                    className={`flex-1 py-2 text-sm font-medium rounded-lg transition-colors ${type === 'EXPENSE' ? 'bg-red-500/10 text-red-400 shadow-sm' : 'text-slate-500 hover:text-slate-300'}`}
                                >
                                    {t('categories.expense')}
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setType('INCOME')}
                                    className={`flex-1 py-2 text-sm font-medium rounded-lg transition-colors ${type === 'INCOME' ? 'bg-blue-500/10 text-blue-400 shadow-sm' : 'text-slate-500 hover:text-slate-300'}`}
                                >
                                    {t('categories.income')}
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('categories.colorLabel')}</label>
                        <div className="flex flex-wrap gap-3">
                            {colors.map(c => (
                                <button
                                    key={c.hex}
                                    type="button"
                                    onClick={() => setColor(c.hex)}
                                    className={`w-10 h-10 rounded-full border-2 transition-all ${color === c.hex ? 'border-white scale-110 shadow-lg' : 'border-transparent hover:scale-105'}`}
                                    style={{ backgroundColor: c.hex }}
                                    title={c.name}
                                />
                            ))}
                        </div>
                    </div>

                    <button
                        type="submit"
                        className={`w-full font-medium py-3.5 rounded-xl transition-all shadow-lg active:scale-[0.98] ${editingId ? 'bg-amber-500 hover:bg-amber-400 text-white shadow-amber-500/20' : 'bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white shadow-emerald-500/20'}`}
                    >
                        {editingId ? t('common.update') : t('common.create')}
                    </button>
                </form>
            </div>

            <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl">
                <h2 className="text-xl font-semibold text-white mb-6">{t('categories.existing') || 'Existing categories'}</h2>

                {loading ? (
                    <div className="flex flex-col items-center justify-center py-20 text-slate-500 animate-pulse">
                        <div className="w-10 h-10 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mb-4"></div>
                        {t('common.loading')}
                    </div>
                ) : categories.length === 0 ? (
                    <div className="text-center py-20 bg-slate-900/20 rounded-3xl border border-dashed border-slate-800">
                        <div className="w-16 h-16 bg-slate-800/50 rounded-full flex items-center justify-center mx-auto mb-4">
                            <svg className="w-8 h-8 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                        </div>
                        <p className="text-slate-400 font-medium">{t('categories.empty')}</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                        {categories.map(cat => (
                            <div
                                key={cat.id}
                                className={`bg-slate-900 border rounded-2xl p-4 flex items-center justify-between group transition-all duration-300 ${editingId === cat.id ? 'border-amber-500 ring-1 ring-amber-500/50 shadow-lg shadow-amber-500/5' : 'border-slate-800 hover:border-slate-700'}`}
                            >
                                <div className="flex items-center gap-3 min-w-0">
                                    <div className="w-3 h-3 rounded-full shrink-0 shadow-[0_0_10px_rgba(0,0,0,0.5)]" style={{ backgroundColor: cat.color || '#10b981' }} />
                                    <div className="flex flex-col min-w-0">
                                        <span className="text-slate-200 font-medium truncate">{cat.name}</span>
                                        <span className={`text-[10px] font-bold uppercase tracking-widest ${(cat.categoryType || cat.type) === 'INCOME' ? 'text-blue-400' : 'text-red-400'}`}>
                                            {(cat.categoryType || cat.type) === 'INCOME' ? t('categories.income') : t('categories.expense')}
                                        </span>
                                    </div>
                                </div>
                                <div className="flex items-center gap-1 opacity-100 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={() => handleEditStart(cat)}
                                        className="text-slate-500 hover:text-amber-400 p-2 rounded-lg bg-slate-800 sm:bg-transparent hover:bg-amber-500/10 transition-colors"
                                        title={t('categories.edit')}
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                                    </button>
                                    <button
                                        onClick={() => handleDeleteCategory(cat.id)}
                                        className="text-slate-500 hover:text-red-400 p-2 rounded-lg bg-slate-800 sm:bg-transparent hover:bg-red-500/10 transition-colors"
                                        title={t('common.delete')}
                                    >
                                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}