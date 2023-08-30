import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';

import { Header } from '@/components/Header';
import { Sidebar } from '@/components/Sidebar';
import { Footer } from '@/components/Footer';

import styles from './App.module.css';

const drawerWidth = 300;

export function App() {
  const [sidebarOpen, setSidebarOpen] = useState<boolean>(false);

  return (
    <>
      <Header action={() => setSidebarOpen(!sidebarOpen)} />
      <Box className={styles.container}>
        <Sidebar drawerWidth={drawerWidth} open={sidebarOpen} />
        <Box
          component="main"
          className={styles.mainContainer}
          sx={{
            marginLeft: {
              xs: sidebarOpen ? `100%` : 0,
              md: sidebarOpen ? `${drawerWidth}px` : 0,
            },
          }}>
          <main>
            <Outlet />
          </main>
        </Box>
      </Box>
      <Box className={styles.footerWrapper} sx={{ boxShadow: 19 }}>
        <Footer />
      </Box>
    </>
  );
}
