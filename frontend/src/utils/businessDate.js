const BUSINESS_TIME_ZONE = 'Asia/Ho_Chi_Minh';

const businessDateFormatter = new Intl.DateTimeFormat('en-US', {
  timeZone: BUSINESS_TIME_ZONE,
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
});

/** Trả về ngày nghiệp vụ Việt Nam theo định dạng YYYY-MM-DD, không bị lệch UTC. */
export const toBusinessDateString = (date = new Date()) => {
  const parts = Object.fromEntries(
    businessDateFormatter.formatToParts(date)
      .filter(part => part.type !== 'literal')
      .map(part => [part.type, part.value])
  );
  return `${parts.year}-${parts.month}-${parts.day}`;
};

export const businessDateAfterDays = (days, date = new Date()) =>
  toBusinessDateString(new Date(date.getTime() + Number(days || 0) * 86_400_000));
