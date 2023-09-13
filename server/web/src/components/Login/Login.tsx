import { useAuth } from '@/state/Auth';
import { Alert, Box, Button, Snackbar, Typography } from '@mui/material';
import { useContext, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import styles from './Login.module.css';
import { LoadingContext } from '@/state/Loading';
import { postLogin } from '@/utils/api/login';
import { FormLogin } from './FormLogin';
import { FormRegister } from './FormRegister';
import { isValidEmail } from '@/utils/validate';
import { postRegister } from '@/utils/api/register';

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
  const [loading, setLoading] = useContext(LoadingContext);

  const from = location.state?.from?.pathname || '/';

  const [isCreatingAccount, setCreatingAccount] = useState<boolean>(false);
  const [showAlert, setShowAlert] = useState<string>('');

  const handleLogin = async (email: string, password: string) => {
    if (!loading) {
      try {
        setLoading(true);

        if (!isValidEmail(email)) {
          setShowAlert('Invalid email');
          throw new Error('Invalid email');
        }

        const loginResponse = await postLogin({
          email: email,
          password: password,
        });

        auth.signin(loginResponse.authToken, () => {
          navigate(from, { replace: true });
        });
      } finally {
        setLoading(false);
      }
    }
  };

  const handleRegister = async (name: string, email: string, password: string, repassword: string) => {
    if (!loading) {
      try {
        setLoading(true);

        if (!isValidEmail(email)) {
          setShowAlert('Invalid email');
          throw new Error('Invalid email');
        }

        if (password !== repassword) {
          setShowAlert(`Passwords don't match`);
          throw new Error(`Passwords don't match`);
        }

        const loginResponse = await postRegister({
          name: name,
          email: email,
          password: password,
        });

        auth.signin(loginResponse.authToken, () => {
          navigate(from, { replace: true });
        });

      } finally {
        setLoading(false);
      }
    }
  };

  return (
    <>
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
          <Typography variant="h6" gutterBottom>
            {isCreatingAccount ? "Create an account" : "Login"}
          </Typography>
        </Box>
        {isCreatingAccount && <FormRegister onHandleButton={handleRegister} />}
        {!isCreatingAccount && <FormLogin onHandleButton={handleLogin} />}
        <Box
          sx={{
            my: 3,
          }}>
          <Button
            onClick={() => setCreatingAccount(!isCreatingAccount)}
            variant="text"
            disableElevation>
            <Typography variant="button">{isCreatingAccount ? "Back" : "Create an account"}</Typography>
          </Button>
        </Box>
      </Box>
      <Snackbar
        open={!!showAlert}
        onClose={(_, reason) => reason !== 'clickaway' && setShowAlert('')}
        autoHideDuration={5000}>
        <Alert severity="error">{showAlert}</Alert>
      </Snackbar>
    </>
  );
}
