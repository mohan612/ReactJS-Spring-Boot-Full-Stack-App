import axios from 'axios';
import customAxiosConfig from './customAxiosConfig/CustomAxiosConfig';

class AuthService {
  constructor() {
    this.api = customAxiosConfig;
  }

  async login(credentials) {
    try {
      const response = await this.api.post('/api/authenticate', credentials);
      
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        if (response.data.user) {
          localStorage.setItem('user', JSON.stringify(response.data.user));
        }
      }
      
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || error.message;
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.clear();
    if (typeof window !== 'undefined' && window.location) {
      window.location.href = '/login';
    }
  }

  isAuthenticated() {
    const token = localStorage.getItem('token');
    return !!token;
  }

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  async register(userData) {
    try {
      const response = await this.api.post('/api/signup', userData);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || error.message;
    }
  }

  async registerBusiness(businessData) {
    try {
      const response = await this.api.post('/api/register-business', businessData);
      return response.data;
    } catch (error) {
      throw error.response?.data?.message || error.message;
    }
  }

  getToken() {
    return localStorage.getItem('token');
  }

  isUserLoggedIn() {
    const user = this.getCurrentUser();
    return user && user.type === 'user';
  }

  isBusinessLoggedIn() {
    const user = this.getCurrentUser();
    return user && user.type === 'business';
  }

  getLoggedInUser() {
    const user = this.getCurrentUser();
    return user ? user.username : '';
  }
}

export default new AuthService();
