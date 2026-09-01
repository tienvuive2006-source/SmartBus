const STORAGE_PREFIX = 'trungnam_seat_hold:';

export const createSeatHoldKey = ({ tripId, seats, returnTripId, returnSeats }) => {
  const outbound = [...(seats || [])].sort().join(',');
  const inbound = [...(returnSeats || [])].sort().join(',');
  return `${tripId || ''}|${outbound}|${returnTripId || ''}|${inbound}`;
};

export const saveSeatHoldSession = (key, value) => {
  if (!key || !value?.expiresAt) return;
  sessionStorage.setItem(`${STORAGE_PREFIX}${key}`, JSON.stringify(value));
};

export const loadSeatHoldSession = key => {
  if (!key) return null;
  const storageKey = `${STORAGE_PREFIX}${key}`;

  try {
    const value = JSON.parse(sessionStorage.getItem(storageKey) || 'null');
    if (!value?.expiresAt || new Date(value.expiresAt).getTime() <= Date.now()) {
      sessionStorage.removeItem(storageKey);
      return null;
    }
    return value;
  } catch {
    sessionStorage.removeItem(storageKey);
    return null;
  }
};

export const clearSeatHoldSession = key => {
  if (key) sessionStorage.removeItem(`${STORAGE_PREFIX}${key}`);
};
