import AuthService from '../../api/AuthService';
import axios from 'axios';

// Mock axios
jest.mock('axios');
const mockedAxios = axios;

describe('AuthService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('login', () => {
    test('should login successfully with valid credentials', async () => {
      const mockResponse = {
        data: {
          token: 'mock-jwt-token',
          user: { id: 1, name: 'Test User', email: 'test@example.com' }
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);

      const credentials = {
        email: 'test@example.com',
        password: 'password123'
      };

      const result = await AuthService.login(credentials);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/authenticate', credentials);
      expect(result).toEqual(mockResponse.data);
      expect(localStorage.getItem('token')).toBe('mock-jwt-token');
      expect(localStorage.getItem('user')).toBe(JSON.stringify(mockResponse.data.user));
    });

    test('should throw error with invalid credentials', async () => {
      const mockError = {
        response: {
          status: 401,
          data: { message: 'Invalid credentials' }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);

      const credentials = {
        email: 'test@example.com',
        password: 'wrongpassword'
      };

      await expect(AuthService.login(credentials)).rejects.toThrow('Invalid credentials');
      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });

    test('should handle network errors', async () => {
      mockedAxios.post.mockRejectedValue(new Error('Network Error'));

      const credentials = {
        email: 'test@example.com',
        password: 'password123'
      };

      await expect(AuthService.login(credentials)).rejects.toThrow('Network Error');
    });
  });

  describe('logout', () => {
    test('should clear localStorage and redirect', () => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', JSON.stringify({ id: 1, name: 'Test User' }));

      AuthService.logout();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    test('should return true when token exists', () => {
      localStorage.setItem('token', 'mock-token');
      expect(AuthService.isAuthenticated()).toBe(true);
    });

    test('should return false when token does not exist', () => {
      expect(AuthService.isAuthenticated()).toBe(false);
    });
  });

  describe('getCurrentUser', () => {
    test('should return user data when user exists', () => {
      const mockUser = { id: 1, name: 'Test User', email: 'test@example.com' };
      localStorage.setItem('user', JSON.stringify(mockUser));

      const result = AuthService.getCurrentUser();
      expect(result).toEqual(mockUser);
    });

    test('should return null when user does not exist', () => {
      const result = AuthService.getCurrentUser();
      expect(result).toBeNull();
    });

    test('should handle malformed user data', () => {
      localStorage.setItem('user', 'invalid-json');

      const result = AuthService.getCurrentUser();
      expect(result).toBeNull();
    });
  });

  describe('register', () => {
    test('should register user successfully', async () => {
      const mockResponse = {
        data: {
          id: 1,
          name: 'Test User',
          email: 'test@example.com'
        }
      };
      
      mockedAxios.post.mockResolvedValue(mockResponse);

      const userData = {
        name: 'Test User',
        email: 'test@example.com',
        password: 'password123'
      };

      const result = await AuthService.register(userData);

      expect(mockedAxios.post).toHaveBeenCalledWith('/api/signup', userData);
      expect(result).toEqual(mockResponse.data);
    });

    test('should handle registration errors', async () => {
      const mockError = {
        response: {
          status: 400,
          data: { message: 'Email already exists' }
        }
      };
      
      mockedAxios.post.mockRejectedValue(mockError);

      const userData = {
        name: 'Test User',
        email: 'existing@example.com',
        password: 'password123'
      };

      await expect(AuthService.register(userData)).rejects.toThrow('Email already exists');
    });
  });

  describe('getToken', () => {
    test('should return token from localStorage', () => {
      localStorage.setItem('token', 'mock-token');
      expect(AuthService.getToken()).toBe('mock-token');
    });

    test('should return null when token does not exist', () => {
      expect(AuthService.getToken()).toBeNull();
    });
  });
});
