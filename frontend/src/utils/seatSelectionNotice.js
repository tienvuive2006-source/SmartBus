const SPECIAL_SEAT_MESSAGES = {
  PRIORITY: {
    type: 'PRIORITY',
    icon: 'star',
    title: 'Bạn đang chọn ghế ưu tiên',
    message: 'Vui lòng ưu tiên ghế này cho người cao tuổi, phụ nữ mang thai, người khuyết tật hoặc hành khách cần hỗ trợ.'
  },
  CHILD: {
    type: 'CHILD',
    icon: 'child_care',
    title: 'Bạn đang chọn ghế dành cho trẻ em',
    message: 'Ghế này được bố trí ưu tiên cho hành khách trẻ em. Vui lòng kiểm tra đúng hành khách sử dụng ghế.'
  }
};

export const getSpecialSeatNotice = seat => SPECIAL_SEAT_MESSAGES[seat?.seatType] || null;
