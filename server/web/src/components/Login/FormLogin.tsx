import { Box, Button, TextField, Typography } from '@mui/material';
import { ChangeEvent, useState } from 'react';

type FormLoginProps = { onHandleButton: (email: string, pass: string) => void }

export function FormLogin({ onHandleButton }: FormLoginProps) {
  const [emailInput, setEmailInput] = useState<string>('');
  const [passwordInput, setPasswordInput] = useState<string>('');

  const emailHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setEmailInput(event.target.value);
  };

  const passwordHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setPasswordInput(event.target.value);
  };

  const disabledButton = passwordInput?.trim() == "" || emailInput?.trim() == "";

  return (
    <>
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
          type='password'
          onChange={passwordHandleChange}
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
        <Button
          onClick={() => onHandleButton(emailInput, passwordInput)}
          variant="contained"
          disableElevation
          disabled={disabledButton}>
          <Typography variant="button">{"Login"}</Typography>
        </Button>
      </Box>
    </>
  )
}
