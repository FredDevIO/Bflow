import { useState, useEffect, useMemo } from 'react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, Cell, PieChart, Pie, Legend } from 'recharts';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';

const COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#f43f5e', '#f59e0b', '#06b6d4', '#ec4899', '#94a3b8'];

export default function AnalyticsPage() {
    const { user, currencySymbol } = useAuth();
    const { t, currentLang } = useTranslation();
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [timeRange, setTimeRange] = useState('analytics.thisyear');
    const [dateStart, setDateStart] = useState('');
    const [dateEnd, setDateEnd] = useState('');

    const fetchData = async () => {
        setLoading(true);
        try {
            const now = new Date();
            let start = '';
            let end = new Date(now.getTime() + (24 * 60 * 60 * 1000)).toISOString().split('T')[0]; // Tomorrow to ensure today is inside

            if (timeRange === 'analytics.thismonth') {
                const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
                const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
                start = firstDay.toISOString().split('T')[0];
                end = lastDay.toISOString().split('T')[0];
            } else if (timeRange === 'analytics.last3months') {
                const d = new Date(now.getFullYear(), now.getMonth() - 2, 1);
                start = d.toISOString().split('T')[0];
            } else if (timeRange === 'analytics.last6months') {
                const d = new Date(now.getFullYear(), now.getMonth() - 5, 1);
                start = d.toISOString().split('T')[0];
            } else if (timeRange === 'analytics.thisyear') {
                start = `${now.getFullYear()}-01-01`;
            } else if (timeRange === 'analytics.custom' && dateStart && dateEnd) {
                start = dateStart;
                end = dateEnd;
            } else {
                start = `${now.getFullYear()}-01-01`;
            }

            console.log(`Analytics Request: range=${timeRange}, start=${start}, end=${end}`);
            
            // Using the specific 'between' endpoint as requested
            const res = await api.get(`/transaction/between/${start}/${end}`);
            
            // The between endpoint returns a direct list of objects according to previous 
            // communications, not a Page object. If it's a Page, we handle both.
            let data = [];
            if (res.data && res.data.content) {
                data = res.data.content;
            } else {
                data = Array.isArray(res.data) ? res.data : [];
            }
            
            setTransactions(data);
        } catch (error) {
            console.error("Error fetching analytics data", error);
            setTransactions([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [timeRange, dateStart, dateEnd]);

    const { categoryData, trendData, stats } = useMemo(() => {
        const catMap = {};
        const trendMap = {};
        let totalIncome = 0;
        let totalExpense = 0;

        // Sort transactions by date for the trend chart
        const sortedTx = [...transactions].sort((a, b) => 
            new Date(a.transactionDate || a.date) - new Date(b.transactionDate || b.date)
        );

        // Pre-fill full month if selected
        const now = new Date();
        if (timeRange === 'analytics.thismonth') {
            const year = now.getFullYear();
            const month = now.getMonth();
            const daysInMonth = new Date(year, month + 1, 0).getDate();
            for (let d = 1; d <= daysInMonth; d++) {
                const dateObj = new Date(year, month, d);
                const dateKey = dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                trendMap[dateKey] = { date: dateKey, income: 0, expense: 0, timestamp: dateObj.getTime() };
            }
        }

        sortedTx.forEach(tx => {
            const amount = tx.amount || 0;
            const type = tx.categoryType || tx.type;
            const txDate = tx.transactionDate || tx.date;

            if (type === 'INCOME') totalIncome += amount;
            if (type === 'EXPENSE') totalExpense += amount;

            // Category distribution (Expense only)
            if (type === 'EXPENSE') {
                const catName = tx.categoryName || tx.category?.name || t('transactions.generalCategory');
                catMap[catName] = (catMap[catName] || 0) + amount;
            }

            // Trend over time (grouped by date)
            const dateObj = new Date(txDate);
            const dateKey = dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
            
            if (!trendMap[dateKey]) {
                trendMap[dateKey] = { date: dateKey, income: 0, expense: 0, timestamp: dateObj.getTime() };
            }
            
            trendMap[dateKey].income += (type === 'INCOME' ? amount : 0);
            trendMap[dateKey].expense += (type === 'EXPENSE' ? amount : 0);
        });

        const catDataArray = Object.keys(catMap).map(name => ({ 
            name, 
            value: Number(catMap[name].toFixed(2)) 
        })).sort((a,b) => b.value - a.value);

        // Ensure trend data is sorted by timestamp
        const trendDataArray = Object.values(trendMap).sort((a,b) => a.timestamp - b.timestamp);

        return { 
            categoryData: catDataArray, 
            trendData: trendDataArray,
            stats: {
                income: totalIncome,
                expense: totalExpense,
                balance: totalIncome - totalExpense,
                savingsRate: totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome * 100).toFixed(1) : 0
            }
        };
    }, [transactions, timeRange, t]);

    const locale = currentLang === 'SPANISH' ? 'es-ES' : 'en-US';
    const formatCurrency = (val) => new Intl.NumberFormat(locale, { style: 'currency', currency: user?.currency || 'USD' }).format(val);

    return (
        <div className="space-y-8 max-w-7xl mx-auto py-2">
            {/* Header & Filters */}
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
                <div className="space-y-1">
                    <h1 className="text-4xl font-extrabold text-white tracking-tight">{t('analytics.title')}</h1>
                    <p className="text-slate-400 font-medium">{t('analytics.subtitle')}</p>
                </div>
                
                <div className="flex flex-wrap items-center gap-3 bg-slate-900/50 p-2 rounded-2xl border border-slate-800 backdrop-blur-sm">
                    <div className="flex bg-slate-950 rounded-xl p-1 border border-slate-800">
                        {['analytics.thismonth', 'analytics.last3months', 'analytics.thisyear', 'analytics.custom'].map(range => (
                            <button
                                key={range}
                                onClick={() => setTimeRange(range)}
                                className={`px-4 py-2 text-xs font-bold rounded-lg transition-all ${timeRange === range ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/20' : 'text-slate-500 hover:text-slate-300'}`}
                            >
                                {t(range)}
                            </button>
                        ))}
                    </div>

                    {timeRange === 'analytics.custom' && (
                        <div className="flex items-center gap-2 animate-in fade-in slide-in-from-right-2 duration-300">
                            <input 
                                type="date" 
                                value={dateStart} 
                                onChange={e => setDateStart(e.target.value)} 
                                className="bg-slate-950 border border-slate-700 text-white rounded-xl px-3 py-2 text-xs outline-none focus:border-emerald-500 [color-scheme:dark]"
                            />
                            <span className="text-slate-700 font-bold">/</span>
                            <input 
                                type="date" 
                                value={dateEnd} 
                                onChange={e => setDateEnd(e.target.value)} 
                                className="bg-slate-950 border border-slate-700 text-white rounded-xl px-3 py-2 text-xs outline-none focus:border-emerald-500 [color-scheme:dark]"
                            />
                        </div>
                    )}
                </div>
            </div>

            {/* Quick Stats Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {[
                    { label: t('analytics.totalIncome'), val: stats.income, color: 'text-emerald-400', bg: 'bg-emerald-500/10', icon: 'M13 7h8m0 0v8m0-8l-8 8-4-4-6 6' },
                    { label: t('analytics.totalExpenses'), val: stats.expense, color: 'text-rose-400', bg: 'bg-rose-500/10', icon: 'M13 17h8m0 0v-8m0 8l-8-8-4 4-6-6' },
                    { label: t('analytics.netBalance'), val: stats.balance, color: stats.balance >= 0 ? 'text-blue-400' : 'text-rose-400', bg: 'bg-blue-500/10', icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2' },
                    { label: t('analytics.savingsScore')?.split(':')[0] || 'Savings rate', val: `${stats.savingsRate}%`, isCurrency: false, color: 'text-amber-400', bg: 'bg-amber-500/10', icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z' }
                ].map((stat, i) => (
                    <div key={i} className="bg-white/[0.02] border border-white/5 rounded-3xl p-5 shadow-xl backdrop-blur-md flex items-center gap-4 group hover:bg-white/[0.04] transition-all">
                        <div className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${stat.bg} ${stat.color} shadow-lg transition-transform group-hover:scale-110`}>
                            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={stat.icon} /></svg>
                        </div>
                        <div className="min-w-0">
                            <p className="text-[10px] font-bold text-slate-500 tracking-widest">{stat.label}</p>
                            <p className={`text-xl font-bold truncate ${stat.color}`}>
                                {stat.isCurrency === false ? stat.val : formatCurrency(stat.val)}
                            </p>
                        </div>
                    </div>
                ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Trend Chart */}
                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl lg:col-span-2 relative overflow-hidden group">
                    <div className="absolute top-0 right-0 p-6 opacity-10 group-hover:opacity-20 transition-opacity">
                        <svg className="w-24 h-24 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" /></svg>
                    </div>
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h2 className="text-xl font-semibold text-white">{t('analytics.trendTitle')}</h2>
                            <p className="text-xs text-slate-500 mt-1">{t('dashboard.incomeVsExpenses')}</p>
                        </div>
                    </div>
                    
                    <div className="h-[350px] w-full">
                        {loading ? (
                            <div className="w-full h-full flex flex-col items-center justify-center text-slate-500 animate-pulse">
                                <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mb-4"></div>
                                {t('common.loading')}
                            </div>
                        ) : trendData.length === 0 ? (
                            <div className="w-full h-full flex flex-col items-center justify-center text-slate-500">
                                <svg className="w-12 h-12 mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2" /></svg>
                                <p className="font-medium">{t('dashboard.noTransactions')}</p>
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={trendData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                    <defs>
                                        <linearGradient id="colorInc" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#10b981" stopOpacity={0.3}/>
                                            <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                                        </linearGradient>
                                        <linearGradient id="colorExp" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.3}/>
                                            <stop offset="95%" stopColor="#f43f5e" stopOpacity={0}/>
                                        </linearGradient>
                                    </defs>
                                    <XAxis dataKey="date" stroke="#475569" tick={{fill: '#475569', fontSize: 10, fontWeight: 600}} dy={10} axisLine={false} tickLine={false} />
                                    <YAxis stroke="#475569" tick={{fill: '#475569', fontSize: 10, fontWeight: 600}} dx={-10} axisLine={false} tickLine={false} tickFormatter={(val) => `${currencySymbol}${val}`} />
                                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                                    <Tooltip 
                                        contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '1.25rem', color: '#f8fafc', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.5)' }}
                                        itemStyle={{ fontSize: '12px' }}
                                    />
                                    <Area type="monotone" dataKey="income" stroke="#10b981" strokeWidth={3} fillOpacity={1} fill="url(#colorInc)" name={t('transactions.income')} activeDot={{ r: 6, stroke: '#020617', strokeWidth: 2 }} />
                                    <Area type="monotone" dataKey="expense" stroke="#f43f5e" strokeWidth={3} fillOpacity={1} fill="url(#colorExp)" name={t('transactions.expense')} activeDot={{ r: 6, stroke: '#020617', strokeWidth: 2 }} />
                                </AreaChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>

                {/* Categories Distribution */}
                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col">
                    <h2 className="text-xl font-semibold text-white mb-2">{t('analytics.categoryTitle')}</h2>
                    <p className="text-xs text-slate-500 mb-8">{t('analytics.distributionDesc')}</p>
                    
                    <div className="flex-1 min-h-[300px] w-full">
                        {loading ? (
                            <div className="w-full h-full flex items-center justify-center text-slate-500">{t('common.loading')}</div>
                        ) : categoryData.length === 0 ? (
                            <div className="w-full h-full flex flex-col items-center justify-center text-slate-500 italic text-sm">
                                <svg className="w-10 h-10 mb-2 opacity-10" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                                {t('analytics.insightEmpty')}
                            </div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={categoryData}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={70}
                                        outerRadius={95}
                                        paddingAngle={4}
                                        dataKey="value"
                                        stroke="none"
                                    >
                                        {categoryData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip 
                                        contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '1rem', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.5)' }}
                                        itemStyle={{ color: '#cbd5e1', fontSize: '12px' }}
                                    />
                                </PieChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                    
                    {/* Simplified Custom Legend */}
                    {!loading && categoryData.length > 0 && (
                        <div className="mt-4 grid grid-cols-2 gap-x-4 gap-y-2">
                            {categoryData.slice(0, 4).map((entry, index) => (
                                <div key={entry.name} className="flex items-center gap-2">
                                    <div className="w-2 h-2 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
                                    <span className="text-[10px] font-medium text-slate-400 truncate">{entry.name}</span>
                                    <span className="text-[10px] font-bold text-slate-200 ml-auto">{formatCurrency(entry.value)}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Ranking List */}
                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl lg:col-span-3">
                    <div className="flex items-center justify-between mb-8">
                        <div>
                            <h2 className="text-xl font-semibold text-white">{t('analytics.categoryTitle')}</h2>
                            <p className="text-xs text-slate-500 mt-1">{t('analytics.distributionDesc')}</p>
                        </div>
                        <div className="bg-slate-900 border border-slate-800 px-4 py-1.5 rounded-xl text-xs font-bold text-slate-400">
                            {t('categories.title')}: {categoryData.length}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {loading ? (
                            Array(3).fill(0).map((_, i) => (
                                <div key={i} className="h-16 bg-slate-900/50 rounded-2xl animate-pulse" />
                            ))
                        ) : categoryData.length === 0 ? (
                            <div className="col-span-full py-10 text-center text-slate-600 italic">{t('analytics.insightEmpty')}</div>
                        ) : (
                            categoryData.map((cat, index) => {
                                const percentage = ((cat.value / stats.expense) * 100).toFixed(1);
                                return (
                                    <div key={cat.name} className="flex flex-col gap-3 group">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <div className="w-2 h-8 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }} />
                                                <div className="min-w-0">
                                                    <p className="text-sm font-bold text-white truncate">{cat.name}</p>
                                                    <p className="text-[10px] font-bold text-slate-500 uppercase">{percentage}% {t('analytics.distributionDesc')?.split(' ')[0]}</p>
                                                </div>
                                            </div>
                                            <span className="text-sm font-black text-white">{formatCurrency(cat.value)}</span>
                                        </div>
                                        <div className="w-full h-1.5 bg-slate-900 rounded-full overflow-hidden">
                                            <div 
                                                className="h-full transition-all duration-1000 ease-out shadow-[0_0_8px_rgba(16,185,129,0.3)]"
                                                style={{ 
                                                    width: `${percentage}%`, 
                                                    backgroundColor: COLORS[index % COLORS.length] 
                                                }}
                                            />
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}