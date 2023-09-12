import { useAuth } from '@/state/Auth';
import { Box, Button, TextField, Typography } from '@mui/material';
import { ChangeEvent, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import styles from './Login.module.css';

export function RequireAuth({ children }: { children: JSX.Element }) {
  let auth = useAuth();
  let location = useLocation();

  if (!auth.authToken) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}

export function Login() {
  const navigate = useNavigate();
  const location = useLocation();
  const auth = useAuth();

  const from = location.state?.from?.pathname || '/';

  const [emailInput, setEmailInput] = useState<string>('');
  const [passwordInput, setPasswordInput] = useState<string>('');

  const handleSubmit = () => {
    auth.signin(emailInput, () => {
      navigate(from, { replace: true });
    });
  };

  const emailHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setEmailInput(event.target.value);
  };

  const passwordHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setPasswordInput(event.target.value);
  };

  const disabledButton = passwordInput?.trim() == "" || emailInput?.trim() == "";

  return (
    <Box
      className={styles.center}
    >
      <Box
        sx={{
          my: 1,
        }}>
        <Typography variant="h4" gutterBottom>
          Xef Server
        </Typography>
      </Box>
      <Box
        sx={{
          my: 3,
        }}>
        <TextField
          id="email"
          label="Email"
          value={emailInput}
          onChange={emailHandleChange}
          size="small"
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </Box>
      <Box
        sx={{
          my: 3,
        }}>
        <TextField
          id="password"
          label="Password"
          value={passwordInput}
          onChange={passwordHandleChange}
          size="small"
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </Box>
      <Button
        onClick={handleSubmit}
        variant="contained"
        disableElevation
        disabled={disabledButton}>
        <Typography variant="button">Login</Typography>
      </Button>
    </Box>
  );
}
