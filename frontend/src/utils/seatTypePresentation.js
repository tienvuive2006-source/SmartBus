const SEAT_TYPE_META = {
  STANDARD: { label: 'Ghế thường', icon: 'airline_seat_recline_normal' },
  PRIORITY: { label: 'Ưu tiên', icon: 'star' },
  CHILD: { label: 'Trẻ em', icon: 'child_care' }
};

export const getSeatTypeMeta = type => SEAT_TYPE_META[type] || SEAT_TYPE_META.STANDARD;

export const formatSeatWithType = (seatNumber, seatTypes = {}) => {
  const type = seatTypes?.[seatNumber] || 'STANDARD';
  return type === 'STANDARD' ? seatNumber : `${seatNumber} (${getSeatTypeMeta(type).label})`;
};
