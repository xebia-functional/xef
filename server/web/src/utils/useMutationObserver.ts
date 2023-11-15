import { type RefObject, useEffect } from 'react';

const defaultOptions: MutationObserverInit = {
  attributes: false,
  characterData: true,
  childList: false,
  subtree: true,
};

export const useMutationObserver = <TElement extends HTMLElement>(
  ref: RefObject<TElement> | undefined,
  callback: MutationCallback,
  options: MutationObserverInit = defaultOptions,
) => {
  useEffect(() => {
    if (ref?.current) {
      const observer = new MutationObserver(callback);
      observer.observe(ref?.current, options);

      return () => {
        observer.disconnect();
      };
    }
  }, [callback, options, ref]);
};
