import {
  createContext,
  useState,
  ReactNode,
  Dispatch,
  SetStateAction,
  useEffect,
} from 'react';

import { noop } from '@/utils/constants';

type SettingsContextType = [Settings, Dispatch<SetStateAction<Settings>>];

export const initialSettings: Settings = {
  apiKey: undefined,
};

const SettingsContext = createContext<SettingsContextType>([
  initialSettings,
  noop,
]);

const SettingsProvider = ({ children }: { children: ReactNode }) => {
  const [settings, setSettings] = useState<Settings>(initialSettings);

  useEffect(() => {
    console.info('Settings changed', { ...settings });
  }, [settings]);

  return (
    <SettingsContext.Provider value={[settings, setSettings]}>
      {children}
    </SettingsContext.Provider>
  );
};

export { SettingsContext, SettingsProvider };
