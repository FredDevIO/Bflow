import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTranslation } from '../context/LanguageContext';
import logoUrl from '../assets/bflow-logo-wb.png';
import Swal from 'sweetalert2';

export default function LoginPage() {
    const [isRegister, setIsRegister] = useState(false);
    const [credentials, setCredentials] = useState({ username: '', email: '', password: '' });
    const [isLoading, setIsLoading] = useState(false);
    const [isError, setIsError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [isSuccess, setIsSuccess] = useState(false);
    const { authenticateUser, setUser, registerUser, fetchUserProfile } = useAuth();
    const { t } = useTranslation();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setIsError(false);
        setErrorMessage('');
        try {
            if (isRegister) {
                await registerUser(credentials.username, credentials.email, credentials.password);
                setIsSuccess(true);

                setTimeout(() => {
                    setIsSuccess(false);
                    setIsRegister(false);
                }, 2000);
            } else {
                await authenticateUser(credentials.username, credentials.password);
                setIsSuccess(true);

                setTimeout(async () => {
                    await fetchUserProfile();
                }, 1000);
            }
        } catch (error) {
            setIsError(true);
            const responseData = error.response?.data;
            let msg = t('login.checkData');

            if (responseData?.messages && Array.isArray(responseData.messages)) {
                setErrorMessage(responseData.messages);
            } else {
                if (typeof responseData === 'string') {
                    msg = responseData;
                } else if (responseData?.message) {
                    msg = responseData.message;
                } else if (responseData?.error) {
                    msg = responseData.error;
                } else if (error.message) {
                    msg = error.message;
                }
                setErrorMessage(msg);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleForgotPassword = (e) => {
        e.preventDefault();
        Swal.fire({
            title: t('login.forgotPasswordTitle'),
            text: t('login.forgotPasswordText'),
            icon: 'info',
            background: '#0f172a',
            color: '#f8fafc',
            confirmButtonColor: '#10b981'
        });
    };

    return (
        <div className="min-h-screen flex bg-slate-950 font-sans text-slate-100 overflow-hidden relative">

            <div className="absolute top-[-10%] left-[-10%] w-96 h-96 bg-emerald-600/20 rounded-full blur-[120px] pointer-events-none" />
            <div className="absolute bottom-[-10%] right-[-5%] w-[500px] h-[500px] bg-teal-600/10 rounded-full blur-[150px] pointer-events-none" />


            <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-12 lg:p-20 relative z-10 border-r border-white/5 bg-slate-900/40 backdrop-blur-sm">
                <div>
                    <div className="flex items-center gap-3 mb-12">
                        <img src={logoUrl} alt="Bflow Logo" className="h-15 w-auto object-contain drop-shadow-[0_0_15px_rgba(16,185,129,0.5)]" />
                        <span className="text-2xl font-bold tracking-tight text-white">Bflow</span>
                    </div>

                    <h1 className="text-5xl font-extrabold leading-tight mb-6">
                        {t('login.heroTitle1')} <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-teal-500">{t('login.heroTitle2')}</span>.
                    </h1>
                    <p className="text-slate-400 text-lg max-w-md leading-relaxed">
                        {t('login.heroSubtitle')}
                    </p>
                </div>

                <div className="space-y-6">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-emerald-500/10 flex items-center justify-center border border-emerald-500/20">
                            <svg className="w-6 h-6 text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                            </svg>
                        </div>
                        <div>
                            <h3 className="text-white font-medium">{t('login.security')}</h3>
                            <p className="text-slate-500 text-sm">{t('login.securityDesc')}</p>
                        </div>
                    </div>
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-teal-500/10 flex items-center justify-center border border-teal-500/20">
                            <svg className="w-6 h-6 text-teal-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                            </svg>
                        </div>
                        <div>
                            <h3 className="text-white font-medium">{t('login.growth')}</h3>
                            <p className="text-slate-500 text-sm">{t('login.growthDesc')}</p>
                        </div>
                    </div>
                </div>
            </div>


            <div className="w-full lg:w-1/2 flex items-center justify-center p-6 relative z-10">
                <div className="w-full max-w-md">

                    <div className="flex lg:hidden items-center justify-center gap-3 mb-10">
                        <img src={logoUrl} alt="Bflow Logo" className="h-10 w-auto object-contain drop-shadow-[0_0_15px_rgba(16,185,129,0.5)]" />
                        <span className="text-3xl font-bold tracking-tight text-white">Bflow</span>
                    </div>

                    <div className="bg-white/[0.03] border border-white/10 rounded-3xl p-8 backdrop-blur-xl shadow-2xl relative overflow-hidden">

                        <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-emerald-500/50 to-transparent" />

                        <div className="text-center mb-8">
                            <h2 className="text-2xl font-bold text-white mb-2">{isRegister ? t('login.createAccount') : t('login.signIn')}</h2>
                            <p className="text-slate-400 text-sm">
                                {isRegister ? t('login.enterDetails') : t('login.signInDesc')}
                            </p>
                        </div>

                        {errorMessage && (
                            <div className="mb-8 p-4 bg-red-500/10 border border-red-500/30 rounded-2xl flex items-start gap-3 text-red-400 text-sm animate-in fade-in slide-in-from-top-2 duration-300">
                                <svg className="w-5 h-5 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                <div className="flex flex-col gap-1.5 overflow-hidden">
                                    {Array.isArray(errorMessage) ? (
                                        errorMessage.map((msg, index) => (
                                            <p key={index} className="leading-tight font-medium break-words">
                                                {msg.split(': ')[1] || msg}
                                            </p>
                                        ))
                                    ) : (
                                        <span className="font-medium break-words">{errorMessage}</span>
                                    )}
                                </div>
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div className="space-y-2">
                                <label className="text-sm font-medium text-slate-300 ml-1">{t('login.username')}</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                        </svg>
                                    </div>
                                    <input
                                        type="text"
                                        placeholder={t('login.usernamePlaceholder')}
                                        className={`w-full pl-11 p-3.5 bg-slate-950/50 border rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 transition-all duration-300 ${isError
                                            ? 'border-red-500 focus:ring-red-500/50 focus:border-red-500 animate-shake'
                                            : 'border-slate-800 focus:ring-emerald-500/50 focus:border-emerald-500'
                                            }`}
                                        value={credentials.username}
                                        onChange={(e) => {
                                            setCredentials({ ...credentials, username: e.target.value });
                                            setIsError(false);
                                            setErrorMessage('');
                                        }}
                                        required
                                    />
                                </div>
                            </div>

                            {isRegister && (
                                <div className="space-y-2">
                                    <label className="text-sm font-medium text-slate-300 ml-1">{t('login.email')}</label>
                                    <div className="relative">
                                        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                            <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                            </svg>
                                        </div>
                                        <input
                                            type="email"
                                            placeholder={t('login.emailPlaceholder')}
                                            className={`w-full pl-11 p-3.5 bg-slate-950/50 border rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 transition-all duration-300 ${isError
                                                ? 'border-red-500 focus:ring-red-500/50 focus:border-red-500 animate-shake'
                                                : 'border-slate-800 focus:ring-emerald-500/50 focus:border-emerald-500'
                                                }`}
                                            value={credentials.email}
                                            onChange={(e) => {
                                                setCredentials({ ...credentials, email: e.target.value });
                                                setIsError(false);
                                                setErrorMessage('');
                                            }}
                                            required
                                        />
                                    </div>
                                </div>
                            )}

                            <div className="space-y-2">
                                <div className="flex justify-between items-center ml-1">
                                    <label className="text-sm font-medium text-slate-300">{t('login.password')}</label>
                                    <button 
                                        type="button"
                                        onClick={handleForgotPassword}
                                        className="text-xs text-emerald-400 hover:text-emerald-300 transition-colors"
                                    >
                                        {t('settings.forgotPassword')}
                                    </button>
                                </div>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                                        <svg className="w-5 h-5 text-slate-500" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                        </svg>
                                    </div>
                                    <input
                                        type="password"
                                        placeholder="••••••••"
                                        className={`w-full pl-11 p-3.5 bg-slate-950/50 border rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 transition-all duration-300 ${isError
                                            ? 'border-red-500 focus:ring-red-500/50 focus:border-red-500 animate-shake'
                                            : 'border-slate-800 focus:ring-emerald-500/50 focus:border-emerald-500'
                                            }`}
                                        value={credentials.password}
                                        onChange={(e) => {
                                            setCredentials({ ...credentials, password: e.target.value });
                                            setIsError(false);
                                            setErrorMessage('');
                                        }}
                                        required={!isSuccess}
                                    />
                                </div>
                            </div>

                            <button
                                type="submit"
                                disabled={isLoading}
                                className="w-full py-3.5 px-4 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-white font-semibold rounded-xl transition-all duration-300 transform active:scale-[0.98] shadow-[0_0_20px_rgba(16,185,129,0.3)] hover:shadow-[0_0_25px_rgba(16,185,129,0.5)] flex items-center justify-center gap-2 group disabled:opacity-70 disabled:cursor-not-allowed"
                            >
                                {isSuccess ? (
                                    <div className="flex flex-col items-center justify-center animate-scale-check">
                                        <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                                        </svg>
                                    </div>
                                ) : isLoading ? (
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                ) : (
                                    <>
                                        {isRegister ? t('login.createAccount') : t('login.signIn')}
                                        <svg className="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                                        </svg>
                                    </>
                                )}
                            </button>
                        </form>

                        <div className="mt-8 text-center text-sm text-slate-400">
                            {isRegister ? t('login.alreadyHaveAccount') : t('login.noAccount')}{' '}
                            <button
                                onClick={() => setIsRegister(!isRegister)}
                                className="text-emerald-400 font-medium hover:text-emerald-300 transition-colors"
                            >
                                {isRegister ? t('login.signIn') : t('login.createAccount')}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}