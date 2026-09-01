import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export const createSeatRealtimeClient = ({ tripIds, onSeatChanged }) => {
  const uniqueTripIds = [...new Set((tripIds || []).filter(Boolean).map(String))];
  const client = new Client({
    webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws`),
    reconnectDelay: 3000,
    onConnect: () => {
      uniqueTripIds.forEach(tripId => {
        client.subscribe(`/topic/trips/${tripId}/seats`, message => {
          try {
            onSeatChanged(JSON.parse(message.body));
          } catch (error) {
            console.error('Không đọc được cập nhật ghế realtime:', error);
          }
        });
      });
    }
  });

  return {
    connect: () => client.activate(),
    disconnect: () => client.deactivate()
  };
};
