const TOKEN_KEY = 'trungnam_realtime_seat_hold_token';

export const getOrCreateSeatHoldToken = () => {
  let token = sessionStorage.getItem(TOKEN_KEY);
  if (!token) {
    token = crypto.randomUUID();
    sessionStorage.setItem(TOKEN_KEY, token);
  }
  return token;
};

export const clearSeatHoldToken = () => sessionStorage.removeItem(TOKEN_KEY);
