import { ref } from 'vue';
import axios from 'axios';

export const locations = [
  "An Giang", "Bà Rịa - Vũng Tàu", "Bạc Liêu", "Bến Tre", "Bình Định", "Bình Dương", "Bình Phước", "Bình Thuận", "Cà Mau", "Cần Thơ", "Đà Nẵng", "Đắk Lắk", "Đắk Nông", "Đồng Nai", "Đồng Tháp", "Gia Lai", "Hà Tĩnh", "Hậu Giang", "Hồ Chí Minh", "Sài Gòn", "Khánh Hòa", "Nha Trang", "Kiên Giang", "Kon Tum", "Lâm Đồng", "Đà Lạt", "Long An", "Nghệ An", "Ninh Thuận", "Phú Yên", "Quảng Bình", "Quảng Nam", "Hội An", "Quảng Ngãi", "Quảng Trị", "Sóc Trăng", "Tây Ninh", "Thanh Hóa", "Thừa Thiên Huế", "Huế", "Tiền Giang", "Trà Vinh", "Vĩnh Long",
  "Bến xe Miền Đông, Hồ Chí Minh", "Bến xe Miền Tây, Hồ Chí Minh", "Bến xe An Sương, Hồ Chí Minh", "Bến xe Chợ Lớn, Hồ Chí Minh",
  "Bến xe Trung tâm Đà Nẵng", "Bến xe Phía Nam, Huế", "Bến xe Phía Bắc, Huế", "Bến xe Trung tâm Cần Thơ", 
  "Bến xe Tam Kỳ, Quảng Nam", "Bến xe Đức Long, Gia Lai", "Bến xe Quy Nhơn, Bình Định", 
  "Bến xe phía Nam Nha Trang", "Bến xe phía Bắc Nha Trang", "Bến xe Liên tỉnh Đà Lạt",
  "Bến xe Rạch Sỏi, Kiên Giang", "Bến xe Vũng Tàu", "Bến xe Phan Thiết"
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
      const res = await axios.get('https://nominatim.openstreetmap.org/search', {
        params: { q: val, format: 'json', addressdetails: 1, countrycodes: 'vn', limit: 5, 'accept-language': 'vi' },
        signal: abortController.signal
      });
      if (res.data && res.data.length > 0) {
        const apiMatches = res.data.map(item => formatAddress(item, val));
        suggestions.value = [...new Set([...localMatches, ...apiMatches])];
      } else if (localMatches.length === 0) {
        suggestions.value = ['@@WARNING@@' + val];
      }
    } catch (e) {
      if (!axios.isCancel(e)) {
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
