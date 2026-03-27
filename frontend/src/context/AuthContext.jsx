import { createContext, useState, useContext, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext();

const currencySymbols = {
    'USD': '$',
    'EUR': '€',
    'GBP': '£',
    'JPY': '¥'
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const currencySymbol = user?.currency ? currencySymbols[user.currency] || '$' : '$';

    const fetchUserProfile = async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setLoading(false);
                return;
            }
            const response = await api.get('/users/me');
            setUser(response.data);
        } catch (error) {
            console.error('Failed to fetch user profile:', error);
            localStorage.removeItem('token');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUserProfile();
    }, []);

    const authenticateUser = async (username, password) => {
        const response = await api.post('/auth/login', { username, password });
        localStorage.setItem('token', response.data.token);
        return response.data;
    };

    const registerUser = async (username, email, password) => {
        const response = await api.post('/auth/register', { username, email, password });
        return response.data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, setUser, loading, currencySymbol, authenticateUser, registerUser, logout, fetchUserProfile }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);