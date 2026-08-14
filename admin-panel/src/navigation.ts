import { useSyncExternalStore } from 'react';

const listeners = new Set<() => void>();

window.addEventListener('popstate', () => listeners.forEach((listener) => listener()));

export function navigate(path: string, replace = false) {
  if (replace) {
    window.history.replaceState(null, '', path);
  } else {
    window.history.pushState(null, '', path);
  }
  listeners.forEach((listener) => listener());
}

export function usePathname() {
  return useSyncExternalStore(
    (listener) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    () => window.location.pathname,
  );
}
