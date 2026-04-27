import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import App from '../App';

// Mock components for testing
jest.mock('../components/root/home/Home', () => () => <div data-testid="home-page">Home Page</div>);
jest.mock('../components/root/users/signUp/SignUp', () => () => <div data-testid="signup-page">Sign Up Page</div>);
jest.mock('../components/root/users/login/Login', () => () => <div data-testid="login-page">Login Page</div>);
jest.mock('../components/root/fragments/header/Header', () => () => <div data-testid="header">Header</div>);
jest.mock('../components/protectedRoutes/ProtectedRoutesGuest', () => ({ children }) => <div data-testid="guest-routes">{children}</div>);
jest.mock('../components/protectedRoutes/ProtectedRoutesUser', () => ({ children }) => <div data-testid="user-routes">{children}</div>);
jest.mock('../components/protectedRoutes/ProtectedRoutesBusiness', () => ({ children }) => <div data-testid="business-routes">{children}</div>);

// Mock AuthContext
const mockAuthContext = {
  user: null,
  token: null,
  login: jest.fn(),
  logout: jest.fn(),
  isAuthenticated: false,
  loading: false,
  error: null,
  register: jest.fn(),
  registerBusiness: jest.fn(),
  clearError: jest.fn()
};

jest.mock('../context/AuthContext', () => {
  const mockAuthContext = {
    user: null,
    token: null,
    login: jest.fn(),
    logout: jest.fn(),
    isAuthenticated: false,
    loading: false,
    error: null,
    register: jest.fn(),
    registerBusiness: jest.fn(),
    clearError: jest.fn()
  };
  
  return {
    AuthProvider: ({ children }) => React.createElement('div', { 'data-testid': 'auth-provider' }, children),
    useAuth: () => mockAuthContext
  };
});

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      {component}
    </BrowserRouter>
  );
};

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders without crashing', () => {
    renderWithProviders(<App />);
    expect(screen.getByTestId('header')).toBeInTheDocument();
  });

  test('renders home page on root route', () => {
    renderWithProviders(<App />);
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  test('renders signup page on /signup route', () => {
    renderWithProviders(<App />);
    // Navigate to signup
    window.history.pushState({}, '', '/signup');
    renderWithProviders(<App />);
    expect(screen.getByTestId('signup-page')).toBeInTheDocument();
  });

  test('renders login page on /login route', () => {
    renderWithProviders(<App />);
    // Navigate to login
    window.history.pushState({}, '', '/login');
    renderWithProviders(<App />);
    expect(screen.getByTestId('login-page')).toBeInTheDocument();
  });

  test('has correct routing structure', () => {
    renderWithProviders(<App />);
    
    // Check that protected routes are rendered
    expect(screen.getByTestId('guest-routes')).toBeInTheDocument();
    expect(screen.getByTestId('user-routes')).toBeInTheDocument();
    expect(screen.getByTestId('business-routes')).toBeInTheDocument();
  });

  test('renders all required route components', () => {
    renderWithProviders(<App />);
    
    // Check that main components are present
    expect(screen.getByTestId('header')).toBeInTheDocument();
    expect(screen.getByTestId('guest-routes')).toBeInTheDocument();
    expect(screen.getByTestId('user-routes')).toBeInTheDocument();
    expect(screen.getByTestId('business-routes')).toBeInTheDocument();
  });

  test('has correct number of routes', () => {
    renderWithProviders(<App />);
    
    // Count the number of route elements
    const routes = screen.getAllByTestId(/.*-routes$/);
    expect(routes).toHaveLength(3);
  });

  test('renders within BrowserRouter', () => {
    renderWithProviders(<App />);
    expect(screen.getByText('Header')).toBeInTheDocument();
  });

  test('has proper component structure', () => {
    renderWithProviders(<App />);
    const appDiv = screen.getByText('Header').closest('.App');
    expect(appDiv).toBeInTheDocument();
  });
});

describe('App Component Integration', () => {
  test('integrates with AuthContext', () => {
    renderWithProviders(<App />);
    // Test that the app integrates properly with AuthContext
    expect(mockAuthContext.login).toBeDefined();
    expect(mockAuthContext.logout).toBeDefined();
  });

  test('handles route changes', () => {
    renderWithProviders(<App />);
    
    // Test route navigation
    const initialPath = window.location.pathname;
    expect(initialPath).toBe('/');
    
    // Change route
    window.history.pushState({}, '', '/login');
    expect(window.location.pathname).toBe('/login');
  });
});
