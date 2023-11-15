import { createContext, useState, ReactNode, Dispatch } from 'react';

import { noop } from '@/utils/constants';

type LoadingContextType = [boolean, Dispatch<boolean>];

const initialLoading = false;

const LoadingContext = createContext<LoadingContextType>([
  initialLoading,
  noop,
]);

const LoadingProvider = ({ children }: { children: ReactNode }) => {
  const [loading, setLoading] = useState<boolean>(false);

  return (
    <LoadingContext.Provider value={[loading, setLoading]}>
      {children}
    </LoadingContext.Provider>
  );
};

export { LoadingContext, LoadingProvider };
