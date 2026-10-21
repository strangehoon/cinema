import { useEffect, useState } from 'react';
import { api } from './api';

type LoadState<T> = { path: string; value: T | null; error: string };

export function useLoad<T>(path: string | null, refreshMs?: number) {
  const [state, setState] = useState<LoadState<T> | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!path) return;
    let active = true;
    let requestId = 0;
    const load = () => {
      const currentRequest = ++requestId;
      api<T>(path)
        .then(value => {
          if (active && currentRequest === requestId) setState({ path, value, error: '' });
        })
        .catch(error => {
          if (active && currentRequest === requestId) {
            setState(previous => ({ path, value: previous?.path === path ? previous.value : null,
              error: (error as Error).message }));
          }
        });
    };
    load();
    const timer = refreshMs ? window.setInterval(load, refreshMs) : undefined;
    return () => { active = false; if (timer) window.clearInterval(timer); };
  }, [path, refreshMs, reloadKey]);

  return {
    value: state?.path === path ? state.value : null,
    error: state?.path === path ? state.error : '',
    reload: () => setReloadKey(value => value + 1),
  };
}
