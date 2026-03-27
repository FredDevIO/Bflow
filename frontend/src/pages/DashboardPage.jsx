import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import api from '../api/axios';

export default function DashboardPage() {
    const { user, currencySymbol } = useAuth();
    const { t, currentLang } = useTranslation();
    const navigate = useNavigate();

    const [totalBalance, setTotalBalance] = useState(0);
    const [totalIncome, setTotalIncome] = useState(0);
    const [totalExpense, setTotalExpense] = useState(0);
    const [recentTransactions, setRecentTransactions] = useState([]);
    const [chartData, setChartData] = useState([]);
    const [loading, setLoading] = useState(true);

    const locale = currentLang === 'SPANISH' ? 'es-ES' : 'en-US';

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const now = new Date();
                const year = now.getFullYear();
                const month = now.getMonth();
                const lastDayDate = new Date(year, month + 1, 0).getDate();
                
                const startStr = `${year}-${String(month + 1).padStart(2, '0')}-01`;
                const endStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDayDate).padStart(2, '0')}`;
                
                const [incomeRes, expenseRes, top5Res, monthlyRes] = await Promise.all([
                    api.get('/transaction/type/income'),
                    api.get('/transaction/type/expense'),
                    api.get('/transaction/top5'),
                    api.get(`/transaction/between/${startStr}/${endStr}`)
                ]);

                const incomes = Array.isArray(incomeRes.data) ? incomeRes.data : [];
                const expenses = Array.isArray(expenseRes.data) ? expenseRes.data : [];
                const recentTxs = Array.isArray(top5Res.data) ? top5Res.data : [];
                const monthlyTxs = Array.isArray(monthlyRes.data) ? monthlyRes.data : [];

                const getAmount = (tx) => tx?.amount || 0;
                const getType = (tx) => tx?.categoryType || tx?.type || '';
                const getDate = (tx) => tx?.transactionDate || tx?.date || '';

                // Total Balance (All time)
                const totalAllInc = incomes.reduce((acc, curr) => acc + getAmount(curr), 0);
                const totalAllExp = expenses.reduce((acc, curr) => acc + getAmount(curr), 0);
                setTotalBalance(totalAllInc - totalAllExp);

                // Monthly metrics for the cards
                let monthlyInc = 0;
                let monthlyExp = 0;
                monthlyTxs.forEach(t => {
                    const amt = getAmount(t);
                    const type = getType(t);
                    if (type === 'INCOME') monthlyInc += amt;
                    if (type === 'EXPENSE') monthlyExp += amt;
                });

                setTotalIncome(monthlyInc);
                setTotalExpense(monthlyExp);
                
                // Set recent transactions (Normalizing fields for the UI)
                setRecentTransactions(recentTxs.map(t => ({
                    ...t,
                    type: getType(t),
                    date: getDate(t)
                })));

                // Chart uses monthly data - Fill all days of the month
                const daysInMonth = new Date(year, month + 1, 0).getDate();
                const dataMap = {};
                
                for (let d = 1; d <= daysInMonth; d++) {
                    const dateLabel = new Date(year, month, d).toLocaleDateString(locale, { month: 'short', day: 'numeric' });
                    dataMap[dateLabel] = { date: dateLabel, income: 0, expense: 0, day: d };
                }

                monthlyTxs.forEach(t => {
                    const dateVal = getDate(t);
                    const dateLabel = new Date(dateVal).toLocaleDateString(locale, { month: 'short', day: 'numeric' });
                    const type = getType(t);
                    
                    if (dataMap[dateLabel]) {
                        if (type === 'INCOME') dataMap[dateLabel].income += getAmount(t);
                        if (type === 'EXPENSE') dataMap[dateLabel].expense += getAmount(t);
                    }
                });
                
                let sortedData = Object.values(dataMap).sort((a, b) => a.day - b.day);
                setChartData(sortedData);

            } catch (err) {
                console.error("Dashboard data load error:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, [locale]);

    const formatCurrency = (val) => new Intl.NumberFormat(locale, { style: 'currency', currency: user?.currency || 'USD' }).format(val);

    return (
        <div className="space-y-6">
            <h1 className="text-3xl font-bold text-white mb-6">{t('dashboard.overview')}</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-white/[0.02] border border-white/5 p-6 rounded-3xl shadow-lg relative overflow-hidden group">
                    <div className="absolute top-0 right-[-10%] w-32 h-32 bg-emerald-500/10 rounded-full blur-2xl group-hover:bg-emerald-500/20 transition-all"></div>
                    <div className="flex items-center gap-4 mb-4">
                        <div className="w-12 h-12 rounded-full bg-slate-900/80 border border-slate-800 flex items-center justify-center">
                            <svg className="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                        </div>
                        <h3 className="text-slate-400 font-medium">{t('dashboard.totalBalance')}</h3>
                    </div>
                    {loading ? <div className="h-8 bg-slate-800 rounded animate-pulse w-1/2"></div> : <p className="text-4xl font-bold text-white">{formatCurrency(totalBalance)}</p>}
                </div>

                <div className="bg-white/[0.02] border border-white/5 p-6 rounded-3xl shadow-lg relative overflow-hidden group">
                    <div className="absolute top-0 right-[-10%] w-32 h-32 bg-blue-500/10 rounded-full blur-2xl group-hover:bg-blue-500/20 transition-all"></div>
                    <div className="flex items-center gap-4 mb-4">
                        <div className="w-12 h-12 rounded-full bg-slate-900/80 border border-slate-800 flex items-center justify-center">
                            <svg className="w-6 h-6 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>
                        </div>
                        <h3 className="text-slate-400 font-medium">{t('dashboard.monthlyIncome')}</h3>
                    </div>
                    {loading ? <div className="h-8 bg-slate-800 rounded animate-pulse w-1/2"></div> : <p className="text-3xl font-bold text-white tracking-tight">{formatCurrency(totalIncome)}</p>}
                </div>

                <div className="bg-white/[0.02] border border-white/5 p-6 rounded-3xl shadow-lg relative overflow-hidden group">
                    <div className="absolute top-0 right-[-10%] w-32 h-32 bg-red-500/10 rounded-full blur-2xl group-hover:bg-red-500/20 transition-all"></div>
                    <div className="flex items-center gap-4 mb-4">
                        <div className="w-12 h-12 rounded-full bg-slate-900/80 border border-slate-800 flex items-center justify-center">
                            <svg className="w-6 h-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 17h8m0 0V9m0 8l-8-8-4 4-6-6" /></svg>
                        </div>
                        <h3 className="text-slate-400 font-medium">{t('dashboard.monthlyExpenses')}</h3>
                    </div>
                    {loading ? <div className="h-8 bg-slate-800 rounded animate-pulse w-1/2"></div> : <p className="text-3xl font-bold text-white tracking-tight">{formatCurrency(totalExpense)}</p>}
                </div>
            </div>

            <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 mt-8">
                <div className="xl:col-span-2 bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl">
                    <h2 className="text-xl font-semibold text-white mb-6">{t('dashboard.analytics')}</h2>
                    <div className="h-80">
                        {loading ? (
                            <div className="w-full h-full flex items-center justify-center text-slate-500">{t('common.loading')}</div>
                        ) : (
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                    <defs>
                                        <linearGradient id="colorInc" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8} />
                                            <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                                        </linearGradient>
                                        <linearGradient id="colorExp" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.8} />
                                            <stop offset="95%" stopColor="#f43f5e" stopOpacity={0} />
                                        </linearGradient>
                                    </defs>
                                    <XAxis dataKey="date" stroke="#64748b" tick={{ fill: '#64748b', fontSize: 12 }} dy={10} axisLine={false} tickLine={false} />
                                    <YAxis stroke="#64748b" tick={{ fill: '#64748b', fontSize: 12 }} dx={-10} axisLine={false} tickLine={false} tickFormatter={(val) => `${currencySymbol}${val}`} />
                                    <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                                    <Tooltip
                                        contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '1rem', color: '#f8fafc' }}
                                        itemStyle={{ color: '#f8fafc' }}
                                    />
                                    <Area type="monotone" dataKey="income" stroke="#3b82f6" strokeWidth={3} fillOpacity={1} fill="url(#colorInc)" name={t('transactions.income')} />
                                    <Area type="monotone" dataKey="expense" stroke="#f43f5e" strokeWidth={3} fillOpacity={1} fill="url(#colorExp)" name={t('transactions.expense')} />
                                </AreaChart>
                            </ResponsiveContainer>
                        )}
                    </div>
                </div>

                <div className="bg-white/[0.02] border border-white/5 rounded-3xl p-6 shadow-2xl flex flex-col">
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-xl font-semibold text-white">{t('dashboard.recentTransactions')}</h2>
                        <button 
                            onClick={() => navigate('/transactions')}
                            className="text-sm text-emerald-400 hover:text-emerald-300 font-medium transition-colors"
                        >
                            {t('dashboard.seeAll')}
                        </button>
                    </div>

                    <div className="flex-1 overflow-y-auto space-y-4 pr-2">
                        {loading ? (
                            <div className="text-slate-500 text-center py-4">{t('common.loading')}</div>
                        ) : recentTransactions.length === 0 ? (
                            <div className="text-slate-500 text-center py-4">{t('dashboard.noTransactions')}</div>
                        ) : (
                            recentTransactions.map((tx) => (
                                <div key={tx.id} className="flex items-center justify-between p-3 rounded-2xl hover:bg-white/[0.04] transition-colors group cursor-pointer border border-transparent hover:border-white/5 gap-3">
                                    <div className="flex items-center gap-3 min-w-0">
                                        <div className={`w-10 h-10 shrink-0 rounded-full flex items-center justify-center shadow-lg ${tx.type === 'INCOME' ? 'bg-blue-500/20 text-blue-400 border border-blue-500/20' : 'bg-red-500/20 text-red-400 border border-red-500/20'}`}>
                                            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                {tx.type === 'INCOME'
                                                    ? <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                                    : <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
                                                }
                                            </svg>
                                        </div>
                                        <div className="min-w-0">
                                            <p className="font-semibold text-slate-200 group-hover:text-white transition-colors truncate">{tx.description || tx.categoryName || tx.category?.name || t('transactions.defaultDesc')}</p>
                                            <p className="text-xs text-slate-500">{new Date(tx.date).toLocaleDateString(locale)}</p>
                                        </div>
                                    </div>
                                    <div className="text-right shrink-0">
                                        <p className={`font-bold tracking-tight whitespace-nowrap ${tx.type === 'INCOME' ? 'text-blue-400' : 'text-red-400'}`}>
                                            {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                                        </p>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}