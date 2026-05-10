import { useEffect, useMemo, useState } from 'react';
import {
  ACTIVE_ELDER_REQUIRED_MESSAGE,
  ensureActiveElderId,
  useUserStore,
} from '../stores/userStore';

export function useActiveElderGuard() {
  const { activeElderId, elderBindings } = useUserStore();
  const [resolvedActiveElderId, setResolvedActiveElderId] = useState<number | null>(activeElderId);

  useEffect(() => {
    let canceled = false;

    const resolve = async () => {
      const nextActiveElderId = await ensureActiveElderId();
      if (!canceled) {
        setResolvedActiveElderId((current) => (current === nextActiveElderId ? current : nextActiveElderId));
      }
    };

    if (activeElderId != null) {
      setResolvedActiveElderId((current) => (current === activeElderId ? current : activeElderId));
      return () => {
        canceled = true;
      };
    }

    void resolve();

    return () => {
      canceled = true;
    };
  }, [activeElderId, elderBindings.length]);

  const activeElder = useMemo(
    () => elderBindings.find((binding) => binding.elderId === resolvedActiveElderId) ?? null,
    [elderBindings, resolvedActiveElderId],
  );

  return {
    activeElderId: resolvedActiveElderId,
    activeElder,
    hasActiveElder: resolvedActiveElderId != null,
    guardMessage: ACTIVE_ELDER_REQUIRED_MESSAGE,
  };
}
