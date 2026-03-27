import { useState } from 'react';
import { useTranslation } from '../context/LanguageContext';
import TransactionManager from '../components/TransactionManager';
import CategoryManager from '../components/CategoryManager';

export default function TransactionsPage() {
    const { t } = useTranslation();
    const [activeTab, setActiveTab] = useState('transactions');

    return (
        <div className="min-h-full w-full relative">
            {}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-[-20%] right-[10%] w-[600px] h-[600px] bg-teal-600/10 rounded-full blur-[150px]" />
                <div className="absolute bottom-[-10%] left-[20%] w-[400px] h-[400px] bg-emerald-600/10 rounded-full blur-[120px]" />
            </div>

            <header className="px-8 pt-10 pb-6 flex flex-col md:flex-row md:justify-between md:items-end md:border-b border-white/5 relative z-10 md:static md:px-12 md:py-10 md:pb-6 gap-4">
                <div>
                    <h1 className="text-3xl font-bold text-white tracking-tight">{t('transactions.hub')}</h1>
                    <p className="text-slate-400 mt-1">{t('transactions.hubDesc')}</p>
                </div>

                {}
                <div className="flex p-1 bg-slate-900/80 backdrop-blur-xl border border-white/10 rounded-xl w-full md:w-auto overflow-hidden">
                    <button
                        onClick={() => setActiveTab('transactions')}
                        className={`flex-1 md:w-32 py-2 px-4 text-sm font-medium rounded-lg transition-colors ${activeTab === 'transactions' ? 'bg-emerald-500 text-white shadow-lg' : 'text-slate-400 hover:text-white hover:bg-white/5'}`}
                    >
                        {t('transactions.tabTransactions')}
                    </button>
                    <button
                        onClick={() => setActiveTab('categories')}
                        className={`flex-1 md:w-32 py-2 px-4 text-sm font-medium rounded-lg transition-colors ${activeTab === 'categories' ? 'bg-emerald-500 text-white shadow-lg' : 'text-slate-400 hover:text-white hover:bg-white/5'}`}
                    >
                        {t('transactions.tabCategories')}
                    </button>
                </div>
            </header>

            <div className="px-8 md:px-12 py-2 flex-1 relative z-10 w-full h-full pb-10">
                <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
                    {activeTab === 'transactions' && <TransactionManager />}
                    {activeTab === 'categories' && <CategoryManager />}
                </div>
            </div>
        </div>
    );
}
