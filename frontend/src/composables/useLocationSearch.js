import { ref } from 'vue';
import axios from 'axios';

export const locations = [
  "Bến xe Miền Đông, Hồ Chí Minh", "Bến xe Miền Tây, Hồ Chí Minh", "Bến xe An Sương, Hồ Chí Minh", "Bến xe Ngã Tư Ga, Hồ Chí Minh",
  "Bến xe Trung tâm Đà Nẵng", "Bến xe Đức Long, Gia Lai", "Bến xe Quy Nhơn, Bình Định", "Bến xe Bồng Sơn, Hoài Nhơn",
  "Bến xe Phía Nam Nha Trang", "Bến xe Phía Bắc Nha Trang", "Bến xe Cam Ranh", "Bến xe Phan Thiết",
  "Bến xe Vĩnh Long", "Bến xe Trung tâm Cần Thơ", "Bến xe Rạch Sỏi, Kiên Giang", "Bến xe Hà Tiên",
  "Bến xe Vũng Tàu", "Bến xe Bà Rịa", "Bến xe Long Điền", "Bến xe Bến Tre", "Bến xe Trà Vinh",
  "Bến xe Cà Mau", "Bến xe Bạc Liêu", "Bến xe Sóc Trăng", "Bến xe Cao Lãnh", "Bến xe Sa Đéc",
  "Bến xe Long Xuyên", "Bến xe Châu Đốc", "Bến xe Tây Ninh", "Bến xe Đồng Xoài", "Bến xe Gia Nghĩa",
  "Bến xe Buôn Ma Thuột", "Bến xe Liên tỉnh Đà Lạt", "Bến xe Kon Tum", "Bến xe Tuy Hòa", 
  "Bến xe Quảng Ngãi", "Bến xe Tam Kỳ, Quảng Nam", "Bến xe Phía Nam, Huế", "Bến xe Phía Bắc, Huế",
  "Bến xe Đồng Hới", "Bến xe Đông Hà, Quảng Trị", "Bến xe Hà Tĩnh", "Bến xe Vinh", "Bến xe Thanh Hóa",
  "Bến xe Ninh Bình", "Bến xe Thái Bình", "Bến xe Nam Định", "Bến xe Phủ Lý", "Bến xe Hòa Bình",
  "Bến xe Mỹ Đình, Hà Nội", "Bến xe Giáp Bát, Hà Nội", "Bến xe Nước Ngầm, Hà Nội", "Bến xe Yên Nghĩa, Hà Nội",
  "Bến xe Gia Lâm, Hà Nội", "Bến xe Thượng Lý, Hải Phòng", "Bến xe Cầu Rào, Hải Phòng",
  "Bến xe Lạng Sơn", "Bến xe Cao Bằng", "Bến xe Hà Giang", "Bến xe Lào Cai", "Bến xe Điện Biên Phủ"
].sort();

export const removeAccents = (str) => {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase();
};

export const formatAddress = (item, originalQuery) => {
  if (!item.address) return item.display_name;
  const parts = [];
  
  const firstPart = item.display_name.split(',')[0].trim();
  
  const name = item.name || item.address.road || item.address.pedestrian || firstPart;
  if (name) parts.push(name);
  
  const suburb = item.address.suburb || item.address.village || item.address.quarter || item.address.city_district || item.address.county;
  if (suburb && suburb !== name) parts.push(suburb);
  
  const city = item.address.city || item.address.state || item.address.province;
  if (city && city !== name) parts.push(city);
  
  if (parts.length === 0) return item.display_name;
  
  if (firstPart !== name && firstPart !== city && firstPart !== suburb) {
     parts.unshift(firstPart);
  }
  
  let formatted = [...new Set(parts)].join(', ');
  
  if (originalQuery) {
    const regex = /^((số|kiệt|ngõ|hẻm)?\s*\d+[a-zA-Z]?(\/\d+[a-zA-Z]?)?\s+)/i;
    const match = originalQuery.match(regex);
    const hasHouseNumber = item.address && item.address.house_number;
    if (match && !hasHouseNumber) {
      formatted = match[0].trim() + ' ' + formatted;
    }
  }
  
  return formatted;
};

export function useLocationSearch() {
  const suggestions = ref([]);
  const isSearching = ref(false);
  const showDropdown = ref(false);
  let abortController = null;

  const performSearch = async (val) => {
    if (!val) {
      suggestions.value = locations;
      return;
    }
    
    const q = removeAccents(val);
    const localMatches = locations.filter(loc => removeAccents(loc).includes(q)).slice(0, 5);
    suggestions.value = localMatches;
    
    if (val.length < 3) return;
    
    if (abortController) abortController.abort();
    abortController = new AbortController();
    
    isSearching.value = true;
    try {
      // 🚌 THUẬT TOÁN TỐI ƯU: Nếu chưa có chữ "Bến xe", tự động thêm vào để quét bến xe nhỏ ở tỉnh
      const searchQuery = val.toLowerCase().includes('ben xe') ? val : `Bến xe ${val}`;
      
      const params = new URLSearchParams({
        q: searchQuery,
        format: 'json',
        addressdetails: '1',
        countrycodes: 'vn',
        limit: '10',
        'accept-language': 'vi',
        featuretype: 'transportation'
      });

      const res = await fetch(`https://nominatim.openstreetmap.org/search?${params.toString()}`, {
        signal: abortController.signal
      });
      const data = await res.json();

      if (data && data.length > 0) {
        // Lọc để ưu tiên các kết quả thực sự là bến xe hoặc trạm xe
        const apiMatches = data
          .filter(item => {
            const dn = item.display_name.toLowerCase();
            return dn.includes('ben xe') || dn.includes('bus station') || dn.includes('tram xe');
          })
          .map(item => formatAddress(item, val));
        
        suggestions.value = [...new Set([...localMatches, ...apiMatches])];
      } else if (localMatches.length === 0) {
        suggestions.value = ['@@WARNING@@' + val];
      }
    } catch (e) {
      if (e.name !== 'AbortError') {
        console.warn("Lỗi tìm kiếm API Địa chỉ:", e);
        if (localMatches.length === 0) suggestions.value = ['@@WARNING@@' + val];
      }
    } finally {
      if (abortController && !abortController.signal.aborted) {
        isSearching.value = false;
      }
    }
  };

  const handleFocus = (val) => {
    showDropdown.value = true;
    performSearch(val);
  };

  const handleSelect = (loc, updateCallback) => {
    const finalValue = loc.replace('@@WARNING@@', '');
    updateCallback(finalValue);
    showDropdown.value = false;
  };
  
  const closeDropdown = () => {
    showDropdown.value = false;
  };

  return {
    suggestions,
    isSearching,
    showDropdown,
    performSearch,
    handleFocus,
    handleSelect,
    closeDropdown
  };
}
