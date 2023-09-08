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

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <ErrorPage />,
    children: [
      {
        path: '/',
        element: <Root />,
      },
      {
        path: '1',
        element: <FeatureOne />,
      },
      {
        path: '2',
        element: <Chat />,
      },
      {
        path: 'generic-question',
        element: <GenericQuestion />,
      },
      {
        path: 'settings',
        element: <SettingsPage />,
      },
    ],
  },
]);

createRoot(document.getElementById('root') as HTMLElement).render(
  <StrictMode>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline enableColorScheme />
        <LoadingProvider>
          <SettingsProvider>
            <RouterProvider router={router} />
          </SettingsProvider>
        </LoadingProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </StrictMode>,
);
