import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Header from '../../components/root/fragments/header/Header';
import { AuthProvider } from '../../context/AuthContext';

// Mock AuthContext
const mockAuthContext = {
  user: null,
  token: null,
  login: jest.fn(),
  logout: jest.fn(),
  isAuthenticated: false
};

jest.mock('../../context/AuthContext', () => ({
  AuthProvider: ({ children }) => (
    <AuthContext.Provider value={mockAuthContext}>
      {children}
    </AuthContext.Provider>
  ),
  useAuth: () => mockAuthContext
}));

const AuthContext = React.createContext(mockAuthContext);

const renderWithProviders = (component) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  );
};

describe('Header Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders header without crashing', () => {
    renderWithProviders(<Header />);
    expect(screen.getByTestId('header')).toBeInTheDocument();
  });

  test('displays navigation links', () => {
    renderWithProviders(<Header />);
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });

  test('shows login link when user is not authenticated', () => {
    mockAuthContext.isAuthenticated = false;
    renderWithProviders(<Header />);
    expect(screen.getByText('Login')).toBeInTheDocument();
  });

  test('shows user menu when user is authenticated', () => {
    mockAuthContext.isAuthenticated = true;
    mockAuthContext.user = { name: 'Test User', type: 'user' };
    renderWithProviders(<Header />);
    expect(screen.getByText('Test User')).toBeInTheDocument();
  });

  test('calls logout function when logout button is clicked', () => {
    mockAuthContext.isAuthenticated = true;
    mockAuthContext.user = { name: 'Test User', type: 'user' };
    renderWithProviders(<Header />);
    
    const logoutButton = screen.getByText('Logout');
    fireEvent.click(logoutButton);
    
    expect(mockAuthContext.logout).toHaveBeenCalledTimes(1);
  });

  test('navigates to home page when logo is clicked', () => {
    renderWithProviders(<Header />);
    const logo = screen.getByAltText('Hobbie Logo');
    fireEvent.click(logo);
    expect(window.location.pathname).toBe('/');
  });
});
