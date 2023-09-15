import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { CssBaseline, StyledEngineProvider } from '@mui/material';
import { ThemeProvider } from '@emotion/react';

import { App } from '@/components/App';
import { Root } from '@/components/Pages/Root';
import { ErrorPage } from '@/components/Pages/ErrorPage';
import { FeatureOne } from '@/components/Pages/FeatureOne';
import { Chat } from '@/components/Pages/Chat';
import { GenericQuestion } from '@/components/Pages/GenericQuestion';
import { SettingsPage } from '@/components/Pages/SettingsPage';

import { LoadingProvider } from '@/state/Loading';
import { SettingsProvider } from '@/state/Settings';

import { theme } from '@/styles/theme';

import './main.css';
import { AuthProvider } from './state/Auth';
import { Login, RequireAuth } from './components/Login';

const router = createBrowserRouter([
  {
    path: '/login',
    errorElement: <ErrorPage />,
    children: [
      {
        path: '/login',
        element: <Login />,
      },
    ]
  },
  {
    path: '/',
    element: <App />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: '/',
        element: (
          <RequireAuth>
            <Root />
          </RequireAuth>
        ),
      },
      {
        path: '1',
        element: (
          <RequireAuth>
            <FeatureOne />
          </RequireAuth>
        ),
      },
      {
        path: '2',
        element: (
          <RequireAuth>
            <Chat initialMessages={[]} />
          </RequireAuth>
        ),
      },
      {
        path: 'generic-question',
        element: (
          <RequireAuth>
            <GenericQuestion />
          </RequireAuth>
        ),
      },
      {
        path: 'settings',
        element: (
          <RequireAuth>
            <SettingsPage />
          </RequireAuth>
        ),
      },
    ],
  },
]);

createRoot(document.getElementById('root') as HTMLElement).render(
  <StrictMode>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline enableColorScheme />
        <AuthProvider>
          <LoadingProvider>
            <SettingsProvider>
              <RouterProvider router={router} />
            </SettingsProvider>
          </LoadingProvider>
        </AuthProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </StrictMode>,
);
