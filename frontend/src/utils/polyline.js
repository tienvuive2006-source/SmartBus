export function decodePolyline(str, precision = 5) { let index = 0, lat = 0, lng = 0, coordinates = []; let shift = 0, result = 0, byte = null; let latitude_change, longitude_change, factor = Math.pow(10, precision); while (index < str.length) { byte = null; shift = 0; result = 0; do { byte = str.charCodeAt(index++) - 63; result |= (byte & 0x1f) << shift; shift += 5; } while (byte >= 0x20); latitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1)); shift = result = 0; do { byte = str.charCodeAt(index++) - 63; result |= (byte & 0x1f) << shift; shift += 5; } while (byte >= 0x20); longitude_change = ((result & 1) ? ~(result >> 1) : (result >> 1)); lat += latitude_change; lng += longitude_change; coordinates.push([lat / factor, lng / factor]); } return coordinates; }

export async function uploadPolylineToCloudinary(polylineStr) {
    const blob = new Blob([polylineStr], { type: 'text/plain' });
    const fd = new FormData();
    fd.append('file', blob, 'route_data.txt');
    fd.append('upload_preset', 'skybus_preset');
    
    try {
        const res = await fetch('https://api.cloudinary.com/v1_1/dzydry2xn/raw/upload', { method: 'POST', body: fd });
        const data = await res.json();
        return data.secure_url;
    } catch (e) {
        console.error('Lỗi upload routeData lên Cloudinary', e);
        return polylineStr; 
    }
}

export async function fetchPolylineFromCloudinary(url) {
    if (!url || !url.startsWith('http')) return url; 
    try {
        const res = await fetch(url);
        return await res.text();
    } catch (e) {
        console.error('Lỗi tải routeData từ Cloudinary', e);
        return '';
    }
}