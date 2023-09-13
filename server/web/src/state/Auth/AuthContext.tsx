import React from 'react';
import { createContext, useState } from 'react';

export function useAuth() {
  return React.useContext(AuthContext);
}

interface AuthContextType {
  authToken: string;
  signin: (authToken: string, callback: VoidFunction) => void;
  signout: (callback: VoidFunction) => void;
}

const AuthContext = createContext<AuthContextType>(null!);

function AuthProvider({ children }: { children: React.ReactNode }) {
  const [authToken, setAuthToken] = useState<string>(sessionStorage.getItem('authToken') || '');

  const signin = (newAuthToken: string, callback: VoidFunction) => {
    setAuthToken(newAuthToken);
    sessionStorage.setItem('authToken', newAuthToken);
    callback();
  };

  const signout = (callback: VoidFunction) => {
    setAuthToken('');
    sessionStorage.setItem('authToken', '');
    callback();
  };

  const value = { authToken, signin, signout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export { AuthContext, AuthProvider };
