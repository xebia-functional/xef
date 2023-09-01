import { ChangeEvent, KeyboardEvent, useContext, useState } from 'react';
import {
  Alert,
  Box,
  IconButton,
  InputAdornment,
  Snackbar,
  TextField,
  Typography,
} from '@mui/material';
import { SendRounded } from '@mui/icons-material';

import { HourglassLoader } from '@/components/HourglassLoader';

import { LoadingContext } from '@/state/Loading';
import { SettingsContext } from '@/state/Settings';

import {
  ApiOptions,
  EndpointsEnum,
  apiConfigConstructor,
  apiFetch,
  defaultApiServer,
} from '@/utils/api';

import styles from './GenericQuestion.module.css';

const baseHeaders = {
  Accept: 'application/json',
  'Content-Type': 'application/json',
};

const chatCompletionsBaseRequest: ChatCompletionsRequest = {
  model: 'gpt-3.5-turbo-16k',
  messages: [
    {
      role: 'user',
      content: '',
      name: 'USER',
    },
  ],
  temperature: 0.4,
  top_p: 1.0,
  n: 1,
  max_tokens: 12847,
  presence_penalty: 0.0,
  frequency_penalty: 0.0,
  logit_bias: {},
  user: 'USER',
};

const chatCompletionsApiBaseOptions: ApiOptions = {
  endpointServer: defaultApiServer,
  endpointPath: EndpointsEnum.chatCompletions,
  endpointValue: '',
  requestOptions: {
    method: 'POST',
    headers: baseHeaders,
  },
};

export function GenericQuestion() {
  const [loading, setLoading] = useContext(LoadingContext);
  const [settings] = useContext(SettingsContext);
  const [prompt, setPrompt] = useState<string>('');
  const [showAlert, setShowAlert] = useState<string>('');
  const [responseMessage, setResponseMessage] =
    useState<ChatCompletionMessage['content']>('');

  const handleClick = async () => {
    if (!loading) {
      try {
        setLoading(true);
        console.group(`🖱️ Generic question form used:`);

        const chatCompletionsRequest: ChatCompletionsRequest = {
          ...chatCompletionsBaseRequest,
          messages: [
            {
              ...chatCompletionsBaseRequest.messages[0],
              content: prompt,
            },
          ],
        };
        const chatCompletionsApiOptions: ApiOptions = {
          ...chatCompletionsApiBaseOptions,
          body: JSON.stringify(chatCompletionsRequest),
          requestOptions: {
            ...chatCompletionsApiBaseOptions.requestOptions,
            headers: {
              ...chatCompletionsApiBaseOptions.requestOptions?.headers,
              Authorization: `Bearer ${settings.apiKey}`,
            },
          },
        };
        const chatCompletionsApiConfig = apiConfigConstructor(
          chatCompletionsApiOptions,
        );
        const chatCompletionResponse = await apiFetch<ChatCompletionsResponse>(
          chatCompletionsApiConfig,
        );
        const { content } = chatCompletionResponse.choices[0].message;

        setResponseMessage(content);

        console.info(`Chat completions request completed`);
      } catch (error) {
        const userFriendlyError = `Chat completions request couldn't be completed`;
        console.info(userFriendlyError);
        setShowAlert(`
              ${userFriendlyError}, is the API key set?`);
      } finally {
        console.groupEnd();
        setLoading(false);
      }
    }
  };

  const handleKey = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleClick();
    }
  };

  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    setPrompt(event.target.value);
  };

  const disabledButton = loading || !prompt.trim();

  return (
    <>
      <Box
        sx={{
          my: 1,
        }}>
        <Typography variant="h4" gutterBottom>
          Generic question
        </Typography>
        <Typography variant="body1" gutterBottom>
          This is an example of a generic call to the xef-server API.
        </Typography>
        <Typography variant="body1">
          Please check that you have your OpenAI key set in the Settings page.
          Then ask any question in the form below:
        </Typography>
      </Box>
      <Box
        sx={{
          my: 3,
        }}>
        <TextField
          id="prompt-input"
          placeholder="e.g: what's the meaning of life?"
          hiddenLabel
          multiline
          maxRows={2}
          value={prompt}
          onChange={handleChange}
          onKeyDown={handleKey}
          disabled={loading}
          InputProps={{
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="send prompt text"
                  color="primary"
                  disabled={disabledButton}
                  title="Send message"
                  onClick={handleClick}>
                  {loading ? <HourglassLoader /> : <SendRounded />}
                </IconButton>
              </InputAdornment>
            ),
          }}
          inputProps={{
            cols: 40,
          }}
        />
      </Box>
      {responseMessage && (
        <>
          <Typography variant="h6">Response:</Typography>
          <Typography variant="body1" className={styles.response}>
            {responseMessage}
          </Typography>
        </>
      )}
      <Snackbar
        open={!!showAlert}
        onClose={(_, reason) => reason !== 'clickaway' && setShowAlert('')}
        autoHideDuration={3000}>
        <Alert severity="error">{showAlert}</Alert>
      </Snackbar>
    </>
  );
}
