const host = window.location.hostname;
const defaultURL = host === 'localhost' ? 'http://localhost:8080/api' : `http://${host}:8080/api`;

export const API_URL = import.meta.env.VITE_API_URL || defaultURL;
