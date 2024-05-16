import {ChangeEvent, KeyboardEvent, useContext, useEffect, useRef, useState} from 'react';
import {
    Alert,
    Box,
    IconButton,
    InputAdornment,
    Snackbar,
    TextField,
    Typography,
} from '@mui/material';
import {SendRounded} from '@mui/icons-material';

import {HourglassLoader} from '@/components/HourglassLoader';

import {LoadingContext} from '@/state/Loading';
import {SettingsContext} from '@/state/Settings';

import styles from './Chat.module.css';

import {openai} from "@/utils/api";

export function Chat({initialMessages = []}: {
    initialMessages: Array<{ role: string, content: string }>
}) {
    const [loading, setLoading] = useContext(LoadingContext);
    const [settings] = useContext(SettingsContext);
    const [prompt, setPrompt] = useState<string>('');
    const [showAlert, setShowAlert] = useState<string>('');
    const [messages, setMessages] = useState(initialMessages);


    const handleClick = async () => {
        if (!loading) {
            try {
                setLoading(true);

                // Add user's message to the conversation first
                setMessages(prevState => [...prevState, {role: 'user', content: prompt}]);

                const client = openai(settings);
                //add header for json content type
                const completion = await client.chat.completions.create({
                    messages: [{role: 'user', content: prompt}],
                    model: 'gpt-3.5-turbo-16k',
                    stream: true
                }, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                let currentAssistantMessage = ''; // Create a local variable to accumulate the message

                // Add an initial placeholder message for the assistant
                setMessages(prevState => [...prevState, {role: 'assistant', content: '...'}]);

                for await (const part of completion) {
                    const text = part.choices[0]?.delta?.content || '';
                    currentAssistantMessage += text;

                    // Update the placeholder message
                    setMessages(prevState => {
                        // Copy all messages except the last one
                        const updatedMessages = [...prevState.slice(0, -1)];
                        // Update the last message with the new content
                        updatedMessages.push({role: 'assistant', content: currentAssistantMessage});
                        return updatedMessages;
                    });
                }

                setPrompt('');

            } catch (error) {
                const userFriendlyError = `Chat completions request couldn't be completed`;
                console.info(userFriendlyError);
                setShowAlert(`${userFriendlyError}. ${error}`);
            } finally {
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

    const messagesEndRef = useRef<HTMLDivElement | null>(null);

    const inputRef = useRef<HTMLInputElement | null>(null);

    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
        }
    }, [messages]);

    useEffect(() => {
        if (!loading && inputRef.current) {
            inputRef.current.focus();
        }
    }, [loading]);

    // ... other code ...

    return (
        <Box className={styles.container}>
            <Box className={styles.messages} sx={{my: 1}}>
                <Typography variant="h4" gutterBottom>
                    Chat Conversation
                </Typography>
                {messages.map((msg, index) => (
                    <Typography key={index} variant="body1"
                                className={msg.role === 'user' ? styles.user : styles.assistant}>
                        {msg.content}
                    </Typography>
                ))}
                <div ref={messagesEndRef} /> {/* This is our scroll-to-bottom anchor */}
            </Box>

            <TextField
                id="chat-input"
                inputRef={inputRef}
                placeholder="Type your message here..."
                hiddenLabel
                multiline
                maxRows={2}
                value={prompt}
                fullWidth={true}
                onChange={handleChange}
                onKeyDown={handleKey}
                disabled={loading}
                InputProps={{
                    endAdornment: (
                        <InputAdornment position="end">
                            <IconButton
                                aria-label="send message"
                                color="primary"
                                disabled={disabledButton}
                                title="Send message"
                                onClick={handleClick}>
                                {loading ? <HourglassLoader/> : <SendRounded/>}
                            </IconButton>
                        </InputAdornment>
                    ),
                }}
                inputProps={{
                    cols: 40,
                }}
            />

            <Snackbar
                open={!!showAlert}
                onClose={(_, reason) => reason !== 'clickaway' && setShowAlert('')}
                autoHideDuration={5000}>
                <Alert severity="error">{showAlert}</Alert>
            </Snackbar>
        </Box>
    );
}

