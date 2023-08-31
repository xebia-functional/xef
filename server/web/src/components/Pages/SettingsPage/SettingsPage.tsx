import { ChangeEvent, useContext, useState } from 'react';
import { Box, Button, TextField, Typography } from '@mui/material';
import { SaveRounded } from '@mui/icons-material';

import { SettingsContext } from '@/state/Settings';

export function SettingsPage() {
  const [settings, setSettings] = useContext(SettingsContext);
  const [apiKeyInput, setApiKeyInput] = useState<string>(settings.apiKey || '');

  const handleSaving = () => {
    setSettings((settings) => ({ ...settings, apiKey: apiKeyInput }));
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setApiKeyInput(event.target.value);
  };

  const disabledButton = settings.apiKey === apiKeyInput?.trim();

  return (
    <>
      <Box
        sx={{
          my: 1,
        }}>
        <Typography variant="h4" gutterBottom>
          Settings
        </Typography>
        <Typography variant="body1">These are xef-server settings.</Typography>
      </Box>
      <Box
        sx={{
          my: 3,
        }}>
        <TextField
          id="api-key-input"
          label="OpenAI API key"
          value={apiKeyInput}
          onChange={handleChange}
          size="small"
          sx={{
            width: { xs: '100%', sm: 550 },
          }}
        />
      </Box>
      <Button
        onClick={handleSaving}
        variant="contained"
        startIcon={<SaveRounded />}
        disableElevation
        disabled={disabledButton}>
        <Typography variant="button">Save settings</Typography>
      </Button>
    </>
  );
}
