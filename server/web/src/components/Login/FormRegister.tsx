import { Box, Button, TextField, Typography } from '@mui/material';
import { ChangeEvent, useState } from 'react';

type FormRegisterProps = {
  onHandleButton: (
    name: string,
    email: string,
    password: string,
    repassword: string
  ) => void
}

export function FormRegister({ onHandleButton }: FormRegisterProps) {
  const [emailInput, setEmailInput] = useState<string>('');
  const [passwordInput, setPasswordInput] = useState<string>('');
  const [rePasswordInput, setRePasswordInput] = useState<string>('');
  const [nameInput, setNameInput] = useState<string>('');

  const emailHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setEmailInput(event.target.value);
  };

  const passwordHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setPasswordInput(event.target.value);
  };

  const rePasswordHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setRePasswordInput(event.target.value);
  };

  const namedHandleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setNameInput(event.target.value);
  };

  const disabledButton = nameInput?.trim() == "" ||
    emailInput?.trim() == "" ||
    passwordInput?.trim() == "" ||
    rePasswordInput?.trim() == "";

  return (
    <>
      <Box
        sx={{
          my: 3,
        }}>
        <TextField
          id="name"
          label="Name"
          value={nameInput}
          onChange={namedHandleChange}
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
        <TextField
          id="repassword"
          label="Re-Password"
          value={rePasswordInput}
          type='password'
          onChange={rePasswordHandleChange}
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
          onClick={() => onHandleButton(nameInput, emailInput, passwordInput, rePasswordInput)}
          variant="contained"
          disableElevation
          disabled={disabledButton}>
          <Typography variant="button">{"Create account"}</Typography>
        </Button>
      </Box>
    </>
  )
}
