import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from '../../components/root/fragments/header/Header';

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

jest.mock('../../context/AuthContext', () => {
  const React = require('react');
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

describe('Header Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders header without crashing', () => {
    renderWithProviders(<Header />);
    expect(screen.getByRole('banner')).toBeInTheDocument();
  });

  test('renders Logo component', () => {
    renderWithProviders(<Header />);
    expect(screen.getByAltText('logo')).toBeInTheDocument();
  });

  test('renders Navbar component', () => {
    renderWithProviders(<Header />);
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });

  test('has correct CSS class', () => {
    renderWithProviders(<Header />);
    const header = screen.getByRole('banner');
    expect(header).toHaveClass('header');
  });

  test('renders navigation menu', () => {
    renderWithProviders(<Header />);
    const menu = screen.getByAltText('mobile');
    expect(menu).toBeInTheDocument();
  });

  test('shows navigation links regardless of auth state', () => {
    mockAuthContext.isAuthenticated = false;
    renderWithProviders(<Header />);
    expect(screen.getByText('Login')).toBeInTheDocument();
    expect(screen.getByText('Register Bizz')).toBeInTheDocument();
  });

  test('shows navigation links regardless of auth state when authenticated', () => {
    mockAuthContext.isAuthenticated = true;
    mockAuthContext.user = { name: 'Test User', type: 'user' };
    renderWithProviders(<Header />);
    expect(screen.getByText('Register Bizz')).toBeInTheDocument();
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  test('maintains consistent layout', () => {
    mockAuthContext.isAuthenticated = false;
    renderWithProviders(<Header />);
    const header = screen.getByRole('banner');
    expect(header).toBeInTheDocument();
    expect(header).toHaveClass('header');
  });

  test('navigates to home page when logo is clicked', () => {
    renderWithProviders(<Header />);
    const logo = screen.getByAltText('logo');
    fireEvent.click(logo);
    expect(window.location.pathname).toBe('/');
  });
});
