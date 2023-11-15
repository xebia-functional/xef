import { AppBar, Box, Button, IconButton, Toolbar, Typography } from '@mui/material';
import { Menu } from '@mui/icons-material';

import logo from '@/assets/xef-brand-name.svg';

import styles from './Header.module.css';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/state/Auth';

export type HeaderProps = {
  action: () => void;
};

export function Header({ action }: HeaderProps) {
  const navigate = useNavigate();
  const auth = useAuth();

  const handleSubmit = () => {
    auth.signout(() => {
      navigate("/login", { replace: true });
    });
  };

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
          <Button
            className={styles.panelRight}
            onClick={handleSubmit}
            variant="text"
            disableElevation>
            <Typography variant="button">Logout</Typography>
          </Button>
        </Toolbar>
      </AppBar>
    </Box>
  );
}
