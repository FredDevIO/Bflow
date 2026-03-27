import { useState, useEffect, useMemo } from 'react';
import api from '../api/axios';
import { useTranslation } from '../context/LanguageContext';
import { useAuth } from '../context/AuthContext';
import Swal from 'sweetalert2';

export default function TransactionManager() {
    const { t, currentLang } = useTranslation();
    const { user } = useAuth();
    const [allTransactions, setAllTransactions] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    
    // Pagination state (for the filtered list)
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 10;

    // Filter & Search State
    const [searchTerm, setSearchTerm] = useState('');
    const [filterType, setFilterType] = useState('ALL');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [isFilterVisible, setIsFilterVisible] = useState(false);

    // Form State (New or Edit)
    const [amount, setAmount] = useState('');
    const [type, setType] = useState('EXPENSE');
    const [categoryId, setCategoryId] = useState('');
    const [description, setDescription] = useState('');
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    
    // Editing state
    const [editingId, setEditingId] = useState(null);

    const fetchData = async () => {
        setLoading(true);
        try {
            // Load ALL transactions to enable perfect client-side filtering and searching
            const [txRes, catRes] = await Promise.all([
                api.get(`/transaction?page=0&size=1000`), // Fetch a large enough batch
                api.get('/categories')
            ]);
            
            if (txRes.data && txRes.data.content) {
                setAllTransactions(txRes.data.content);
            } else {
                setAllTransactions(Array.isArray(txRes.data) ? txRes.data : []);
            }
            
            setCategories(catRes.data || []);
        } catch (error) {
            console.error('Error fetching data:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // Derived State: Filtered Transactions
    const filteredTransactions = useMemo(() => {
        return allTransactions.filter(tx => {
            const txType = tx.categoryType || tx.type || 'EXPENSE';
            const txDesc = (tx.description || '').toLowerCase();
            const txCat = (tx.categoryName || tx.category?.name || '').toLowerCase();
            const txDateStr = tx.transactionDate || tx.date;
            
            const matchesSearch = !searchTerm || 
                txDesc.includes(searchTerm.toLowerCase()) || 
                txCat.includes(searchTerm.toLowerCase());
            
            const matchesType = filterType === 'ALL' || txType === filterType;
            
            const matchesDate = (!startDate || txDateStr >= startDate) && 
                                (!endDate || txDateStr <= endDate);
            
            return matchesSearch && matchesType && matchesDate;
        }).sort((a, b) => new Date(b.transactionDate || b.date) - new Date(a.transactionDate || a.date));
    }, [allTransactions, searchTerm, filterType, startDate, endDate]);

    // Derived State: Paged view of filtered transactions
    const pagedTransactions = useMemo(() => {
        const start = currentPage * pageSize;
        return filteredTransactions.slice(start, start + pageSize);
    }, [filteredTransactions, currentPage]);

    const totalPages = Math.ceil(filteredTransactions.length / pageSize);

    // Reset pagination when search changes
    useEffect(() => {
        setCurrentPage(0);
    }, [searchTerm, filterType, startDate, endDate]);

    const resetForm = () => {
        setAmount('');
        setDescription('');
        setCategoryId('');
        setDate(new Date().toISOString().split('T')[0]);
        setEditingId(null);
        setType('EXPENSE');
    };

    const handleEditStart = (tx) => {
        const txType = tx.categoryType || tx.type || 'EXPENSE';
        const catId = tx.categoryId || tx.category?.id || '';
        const txDate = tx.transactionDate || tx.date || new Date().toISOString().split('T')[0];

        setEditingId(tx.id);
        setAmount(tx.amount.toString());
        setDescription(tx.description || '');
        setType(txType);
        
        let resolvedCatId = catId;
        if (!resolvedCatId && (tx.categoryName || tx.category?.name)) {
            const nameToFind = tx.categoryName || tx.category?.name;
            const found = categories.find(c => c.name === nameToFind);
            if (found) resolvedCatId = found.id;
        }
        setCategoryId(resolvedCatId ? resolvedCatId.toString() : '');
        setDate(txDate);
        
        document.getElementById('transaction-form-top')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!amount || isNaN(amount) || amount <= 0) {
            return Swal.fire({ 
                title: t('common.error'), 
                text: t('common.invalidAmount'), 
                icon: 'warning', 
                background: '#0f172a', 
                color: '#f8fafc', 
                confirmButtonColor: '#3b82f6' 
            });
        }

        const payload = {
            amount: parseFloat(amount),
            description,
            transactionDate: date,
            categoryId: categoryId ? parseInt(categoryId) : null
        };

        try {
            if (editingId) {
                await api.put(`/transaction/${editingId}`, payload);
            } else {
                await api.post('/transaction', payload);
            }
            
            resetForm();
            fetchData();
            
            Swal.fire({
                title: t('common.success'),
                text: editingId ? t('transactions.updated') : t('transactions.saved'),
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
                text: error.response?.data?.message || t('transactions.errorSave', [error.message]),
                icon: 'error',
                background: '#0f172a',
                color: '#f8fafc',
                confirmButtonColor: '#ef4444'
            });
        }
    };

    const handleDelete = async (id) => {
        const result = await Swal.fire({
            title: t('transactions.confirmDeleteTitle'),
            text: t('transactions.confirmDelete'),
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
                await api.delete(`/transaction/${id}`);
                fetchData();
                Swal.fire({
                    title: t('common.deleted'),
                    text: t('transactions.deletedMsg'),
                    icon: 'success',
                    background: '#0f172a',
                    color: '#f8fafc',
                    confirmButtonColor: '#10b981',
                    timer: 1500,
                    showConfirmButton: false
                });
            } catch (error) {
                Swal.fire({
                    title: t('common.error'),
                    text: error.response?.data?.message || t('transactions.errorDelete'),
                    icon: 'error',
                    background: '#0f172a',
                    color: '#f8fafc',
                    confirmButtonColor: '#ef4444'
                });
            }
        }
    };

    const formatCurrency = (val) => {
        const locale = currentLang === 'SPANISH' ? 'es-ES' : 'en-US';
        return new Intl.NumberFormat(locale, { style: 'currency', currency: user?.currency || 'USD' }).format(val);
    };

    const clearFilters = () => {
        setSearchTerm('');
        setFilterType('ALL');
        setStartDate('');
        setEndDate('');
    };

    return (
        <div className="space-y-8 animate-in fade-in zoom-in-95 duration-300 xl:flex xl:gap-8 xl:space-y-0">
            {/* Form Section */}
            <div className="xl:w-1/3 space-y-6">
                <div id="transaction-form-top" className={`bg-white/[0.02] border rounded-3xl p-6 shadow-2xl relative overflow-hidden backdrop-blur-md transition-all duration-300 ${editingId ? 'border-amber-500/50' : 'border-white/5'}`}>
                    {editingId && (
                        <div className="absolute top-4 right-6">
                            <button onClick={resetForm} className="text-amber-500 hover:text-amber-400 text-xs font-semibold underline">{t('common.cancel')}</button>
                        </div>
                    )}
                    <h2 className="text-xl font-semibold text-white mb-6">{editingId ? t('transactions.edit') : t('transactions.new')}</h2>
                    
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div className="flex bg-slate-900 rounded-xl p-1 border border-slate-800">
                            <button
                                type="button"
                                onClick={() => setType('EXPENSE')}
                                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-colors ${type === 'EXPENSE' ? 'bg-red-500/10 text-red-400 shadow-sm' : 'text-slate-500 hover:text-slate-300'}`}
                            >
                                {t('transactions.expense')}
                            </button>
                            <button
                                type="button"
                                onClick={() => setType('INCOME')}
                                className={`flex-1 py-2 text-sm font-medium rounded-lg transition-colors ${type === 'INCOME' ? 'bg-blue-500/10 text-blue-400 shadow-sm' : 'text-slate-500 hover:text-slate-300'}`}
                            >
                                {t('transactions.income')}
                            </button>
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('transactions.amountLabel')}</label>
                            <div className="relative">
                                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 font-medium">{user?.currency || 'USD'}</span>
                                <input 
                                    type="number" 
                                    step="0.01"
                                    min="0"
                                    className="w-full bg-slate-950/50 border border-slate-800 text-white rounded-xl pl-14 pr-4 py-3 outline-none focus:border-emerald-500 transition-colors"
                                    placeholder={t('transactions.amountPlaceholder')}
                                    value={amount}
                                    onChange={(e) => setAmount(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('transactions.dateLabel')}</label>
                            <input 
                                type="date" 
                                className="w-full bg-slate-950/50 border border-slate-800 text-white rounded-xl px-4 py-3 outline-none focus:border-emerald-500 transition-colors [color-scheme:dark]"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('transactions.categoryLabel')}</label>
                            <select 
                                className="w-full bg-slate-950/50 border border-slate-800 text-white rounded-xl px-4 py-3 outline-none focus:border-emerald-500 transition-colors"
                                value={categoryId}
                                onChange={(e) => setCategoryId(e.target.value)}
                                required
                            >
                                <option value="" disabled>{t('transactions.selectCategory')}</option>
                                {categories
                                    .filter(c => (c.categoryType || c.type) === type)
                                    .map(c => (
                                        <option key={c.id} value={c.id}>{c.name}</option>
                                    ))}
                            </select>
                        </div>

                        <div className="space-y-2">
                            <label className="text-xs font-medium text-slate-400 tracking-wider ml-1">{t('transactions.descOptional')}</label>
                            <input 
                                type="text" 
                                className="w-full bg-slate-950/50 border border-slate-800 text-white rounded-xl px-4 py-3 outline-none focus:border-emerald-500 transition-colors"
                                placeholder={t('transactions.whatFor')}
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                        </div>

                        <button 
                            type="submit"
                            className={`w-full font-medium py-3.5 rounded-xl transition-all shadow-lg active:scale-[0.98] ${editingId ? 'bg-amber-500 hover:bg-amber-400 shadow-amber-500/20 text-white' : (type === 'EXPENSE' ? 'bg-gradient-to-r from-red-500 to-rose-600 hover:from-red-400 hover:to-rose-500 text-white shadow-red-500/20' : 'bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-400 hover:to-indigo-500 text-white shadow-blue-500/20')}`}
                        >
                            {editingId ? t('common.update') : (type === 'EXPENSE' ? t('transactions.newExpense') : t('transactions.newIncome'))}
                        </button>
                    </form>
                </div>
            </div>

            {/* List Section */}
            <div className="xl:w-2/3 bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col min-h-[600px]">
                {/* Search & Global Filters */}
                <div className="space-y-4 mb-6">
                    <div className="flex items-center justify-between">
                        <h2 className="text-xl font-semibold text-white">{t('transactions.history')}</h2>
                        <div className="flex items-center gap-2">
                            <button 
                                onClick={() => setIsFilterVisible(!isFilterVisible)}
                                className={`p-2 rounded-lg border transition-all ${isFilterVisible || startDate || endDate ? 'bg-emerald-500/10 border-emerald-500/50 text-emerald-400' : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white'}`}
                                title={t('transactions.dateFilters')}
                            >
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                            </button>
                            {filteredTransactions.length > 0 && (
                                <span className="text-xs text-slate-500 bg-slate-900 border border-slate-800 px-3 py-1 rounded-full whitespace-nowrap">
                                    {t('transactions.matches')}: {filteredTransactions.length}
                                </span>
                            )}
                        </div>
                    </div>

                    <div className="flex flex-col md:flex-row gap-4">
                        <div className="flex-1 relative group">
                            <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500 group-focus-within:text-emerald-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                            <input 
                                type="text"
                                placeholder={t('transactions.fastSearch')}
                                className="w-full bg-slate-900 border border-slate-800 text-white rounded-xl pl-10 pr-4 py-2.5 outline-none focus:border-emerald-500/50 transition-all shadow-inner"
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                        </div>

                        <div className="flex bg-slate-900 rounded-xl p-1 border border-slate-800 self-start">
                            {['ALL', 'INCOME', 'EXPENSE'].map(fType => (
                                <button
                                    key={fType}
                                    onClick={() => setFilterType(fType)}
                                    className={`px-4 py-1.5 text-xs font-semibold rounded-lg transition-all ${filterType === fType ? 'bg-slate-700 text-white shadow-lg' : 'text-slate-500 hover:text-slate-300'}`}
                                >
                                    {fType === 'ALL' ? t('common.all') : (fType === 'INCOME' ? t('transactions.income') : t('transactions.expense'))}
                                </button>
                            ))}
                        </div>
                    </div>

                    {isFilterVisible && (
                        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 animate-in slide-in-from-top-2 duration-300">
                            <div className="space-y-1">
                                <label className="text-[10px] font-bold text-slate-500 ml-1">{t('analytics.from')}</label>
                                <input 
                                    type="date"
                                    className="w-full bg-slate-950 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500/50 [color-scheme:dark]"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                />
                            </div>
                            <div className="space-y-1">
                                <label className="text-[10px] font-bold text-slate-500 ml-1">{t('analytics.to')}</label>
                                <input 
                                    type="date"
                                    className="w-full bg-slate-950 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm outline-none focus:border-emerald-500/50 [color-scheme:dark]"
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                />
                            </div>
                            <div className="flex items-end">
                                <button 
                                    onClick={clearFilters}
                                    className="w-full py-2 text-xs font-semibold text-slate-400 hover:text-white border border-slate-800 rounded-lg hover:bg-slate-800 transition-all"
                                >
                                    {t('transactions.reset')}
                                </button>
                            </div>
                        </div>
                    )}
                </div>
                
                <div className="flex-1 overflow-y-auto pr-2 space-y-3 custom-scrollbar min-h-[400px]">
                    {loading ? (
                        <div className="flex flex-col items-center justify-center py-20 text-slate-500 animate-pulse">
                            <div className="w-10 h-10 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mb-4"></div>
                            {t('common.loading')}
                        </div>
                    ) : pagedTransactions.length === 0 ? (
                        <div className="text-center py-20 bg-slate-900/20 rounded-3xl border border-dashed border-slate-800">
                            <div className="w-16 h-16 bg-slate-800/50 rounded-full flex items-center justify-center mx-auto mb-4">
                                <svg className="w-8 h-8 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 9.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
                            </div>
                            <p className="text-slate-400 font-medium">{t('transactions.empty')}</p>
                            <button onClick={clearFilters} className="text-emerald-500 text-sm mt-2 hover:underline">{t('transactions.reset')}</button>
                        </div>
                    ) : (
                        pagedTransactions.map(tx => (
                            <div key={tx.id} className={`bg-slate-900/40 border rounded-2xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 group hover:bg-slate-900/60 transition-all ${editingId === tx.id ? 'border-amber-500/50 bg-amber-500/5 shadow-lg shadow-amber-500/5' : 'border-slate-800 hover:border-slate-700'}`}>
                                <div className="flex items-center gap-4 min-w-0">
                                    <div className={`w-12 h-12 shrink-0 rounded-full flex items-center justify-center shadow-lg ${(tx.categoryType || tx.type) === 'INCOME' ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20' : 'bg-red-500/10 text-red-400 border border-red-500/20'}`}>
                                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            {(tx.categoryType || tx.type) === 'INCOME'
                                                ? <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                                : <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
                                            }
                                        </svg>
                                    </div>
                                    <div className="overflow-hidden">
                                        <p className="font-semibold text-white truncate max-w-[250px]">{tx.description || tx.categoryName || t('transactions.manualEntry')}</p>
                                        <div className="flex items-center gap-2 mt-0.5">
                                            <span className="text-xs text-slate-500">{new Date(tx.transactionDate || tx.date).toLocaleDateString()}</span>
                                            <span className="w-1 h-1 rounded-full bg-slate-700"></span>
                                            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 border border-slate-700 truncate capitalize font-medium">
                                                {tx.categoryName || tx.category?.name || t('transactions.generalCategory')}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div className="flex items-center justify-between sm:justify-end gap-3 w-full sm:w-auto mt-2 sm:mt-0 pt-3 sm:pt-0 border-t sm:border-0 border-slate-800">
                                    <span className={`font-bold text-lg tracking-tight mr-3 ${(tx.categoryType || tx.type) === 'INCOME' ? 'text-blue-400' : 'text-red-400'}`}>
                                        {(tx.categoryType || tx.type) === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                                    </span>
                                    <div className="flex items-center gap-1">
                                        <button 
                                            onClick={() => handleEditStart(tx)}
                                            className="text-slate-500 hover:text-amber-400 p-2 rounded-lg bg-slate-900 sm:bg-transparent hover:bg-amber-500/10 transition-colors"
                                            title={t('transactions.edit')}
                                        >
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                                        </button>
                                        <button 
                                            onClick={() => handleDelete(tx.id)}
                                            className="text-slate-500 hover:text-red-400 p-2 rounded-lg bg-slate-900 sm:bg-transparent hover:bg-red-500/10 transition-colors"
                                            title={t('common.delete')}
                                        >
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Local Pagination Controls */}
                {totalPages > 1 && (
                    <div className="mt-6 flex items-center justify-center gap-2 pt-6 border-t border-white/5">
                        <button 
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(p => p - 1)}
                            className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-slate-400 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg>
                        </button>
                        
                        <div className="flex items-center gap-1 overflow-x-auto max-w-[200px] sm:max-w-none no-scrollbar py-1">
                            {[...Array(totalPages)].map((_, i) => (
                                <button 
                                    key={i}
                                    onClick={() => setCurrentPage(i)}
                                    className={`w-8 h-8 shrink-0 rounded-lg text-xs font-medium transition-all ${currentPage === i ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/20' : 'bg-slate-900 border border-slate-800 text-slate-500 hover:text-white'}`}
                                >
                                    {i + 1}
                                </button>
                            ))}
                        </div>

                        <button 
                            disabled={currentPage === totalPages - 1}
                            onClick={() => setCurrentPage(p => p + 1)}
                            className="p-2 rounded-lg bg-slate-900 border border-slate-800 text-slate-400 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                        >
                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}