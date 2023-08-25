import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { CssBaseline, StyledEngineProvider } from '@mui/material';
import { ThemeProvider } from '@emotion/react';

import { App } from '@/components/App';
import { Root } from '@/components/Features/Root';
import { ErrorPage } from '@/components/ErrorPage';
import { FeatureOne } from '@/components/Features/FeatureOne';
import { FeatureTwo } from '@/components/Features/FeatureTwo';

import { LoadingProvider } from '@/state/Loading';

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
        element: <FeatureTwo />,
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
          <RouterProvider router={router} />
        </LoadingProvider>
      </ThemeProvider>
    </StyledEngineProvider>
  </StrictMode>,
);
