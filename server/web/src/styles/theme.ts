import { ThemeOptions, createTheme } from '@mui/material';

import '@/styles/typography.css';

const paletteThemeOptions = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#01a3ff',
      dark: '#000b1b',
    },
    secondary: {
      main: '#01c9bd',
    },
    text: {
      primary: '#000b1b',
    },
  },
});

const themeOptions: ThemeOptions = {
  ...paletteThemeOptions,
  components: {
    MuiAppBar: {
      defaultProps: {
        color: 'default',
      },
      styleOverrides: {
        root: {
          color: paletteThemeOptions.palette.text.primary,
        },
      },
    },
    MuiButtonBase: {
      defaultProps: {
        disableRipple: true,
      },
    },
    MuiButton: {
      defaultProps: {
        size: 'large',
      },
      styleOverrides: {
        contained: {
          background:
            'linear-gradient(62.54deg, #01c9bd 9.29%, #018ddc 91.64%)',
          border: 0,
          color: 'white',
          transition:
            'background-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms, box-shadow 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms, border-color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,color 250ms cubic-bezier(0.4, 0, 0.2, 1) 0ms,opacity 233ms cubic-bezier(0.4, 0, 0.2, 1),filter 333ms cubic-bezier(0.4, 0, 0.2, 1)',
          '&:hover': {
            filter: 'hue-rotate(33deg)',
          },
        },
      },
    },
    MuiButtonGroup: {
      defaultProps: {
        size: 'medium',
      },
    },
    MuiCheckbox: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiFab: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiFormControl: {
      defaultProps: {
        margin: 'none',
        size: 'medium',
      },
    },
    MuiFormHelperText: {
      defaultProps: {
        margin: 'dense',
      },
      styleOverrides: {
        root: {
          fontSize: '0.9rem',
          marginTop: '1rem',
        },
      },
    },
    MuiIconButton: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiInputBase: {
      defaultProps: {
        margin: 'dense',
      },
    },
    MuiInputLabel: {
      defaultProps: {
        margin: 'dense',
      },
    },
    MuiList: {
      defaultProps: {
        dense: true,
      },
    },
    MuiMenuItem: {
      defaultProps: {
        dense: true,
      },
    },
    MuiTable: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          transition: 'all 300ms cubic-bezier(0.4, 0, 0.2, 1)',
        },
      },
    },
    MuiRadio: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiSwitch: {
      defaultProps: {
        size: 'small',
      },
    },
    MuiTextField: {
      defaultProps: {
        margin: 'none',
        size: 'medium',
      },
    },
    MuiTypography: {
      styleOverrides: {
        h1: {
          fontWeight: 600,
        },
        h2: {
          fontWeight: 600,
        },
        h3: {
          fontWeight: 600,
        },
        h4: {
          fontWeight: 600,
        },
        h5: {
          fontWeight: 600,
        },
        h6: {
          fontWeight: 600,
        },
        button: {
          fontWeight: 600,
        },
      },
    },
  },
  typography: {
    fontFamily:
      "'Inter', open sans, sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol'",
    button: {
      textTransform: 'capitalize',
    },
  },
  spacing: 8,
  shape: {
    borderRadius: 6,
  },
};

export const theme = createTheme(themeOptions);
