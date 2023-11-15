import {
    createContext,
    useState,
    ReactNode,
    Dispatch,
    SetStateAction,
    useEffect,
} from 'react';

import {noop} from '@/utils/constants';

export type Settings = {
    apiKey?: string;
}

type SettingsContextType = [Settings, Dispatch<SetStateAction<Settings>>];

export const initialSettings: Settings = {
    apiKey: sessionStorage.getItem('apiKey') || '',
};

const SettingsContext = createContext<SettingsContextType>([
    initialSettings,
    noop,
]);

const SettingsProvider = ({children}: { children: ReactNode }) => {
    const [settings, setSettings] = useState<Settings>(initialSettings);

    useEffect(() => {
        console.info('Settings changed', {...settings});
        sessionStorage.setItem('apiKey', settings.apiKey || '');
    }, [settings]);

    return (
        <SettingsContext.Provider value={[settings, setSettings]}>
            {children}
        </SettingsContext.Provider>
    );
};

export {SettingsContext, SettingsProvider};
