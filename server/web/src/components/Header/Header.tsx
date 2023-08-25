import { AppBar, Box, IconButton, Toolbar, Typography } from '@mui/material';
import { Menu } from '@mui/icons-material';

import logo from '@/assets/xef-brand-name.svg';

import styles from './Header.module.css';

export type HeaderProps = {
  action: () => void;
};

export function Header({ action }: HeaderProps) {
  return (
    <Box className={styles.container} sx={{ flexGrow: 1 }}>
      <AppBar position="fixed">
        <Toolbar>
          <IconButton
            size="large"
            edge="start"
            color="inherit"
            aria-label="menu"
            onClick={action}
            sx={{ mr: 2 }}>
            <Menu />
          </IconButton>
          <img className={styles.logo} src={logo} alt="Logo" />
          <Typography variant="h5" component="div" sx={{ flexGrow: 1 }}>
            Dashboard
          </Typography>
        </Toolbar>
      </AppBar>
    </Box>
  );
}
