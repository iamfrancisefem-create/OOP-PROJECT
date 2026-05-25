import { Session, toast } from './utils.js';

const WS_BASE = 'ws://localhost:8080/ws';

class ChatSocket {
  constructor() {
    this.ws = null;
    this.reconnectTimer = null;
    this.listeners = new Map();
  }

  connect(path = '/chat') {
    if (this.ws?.readyState === WebSocket.OPEN) return;
    const token = Session.getToken();
    if (!token) return;

    try {
      this.ws = new WebSocket(`${WS_BASE}${path}?token=${token}`);

      this.ws.onopen = () => {
        console.log('[WebSocket] Connected');
        this._emit('connected');
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this._emit('message', data);
        } catch (e) {
          this._emit('raw', event.data);
        }
      };

      this.ws.onclose = (event) => {
        console.log('[WebSocket] Disconnected:', event.code);
        this._emit('disconnected');
        this._scheduleReconnect(path);
      };

      this.ws.onerror = (err) => {
        console.error('[WebSocket] Error:', err);
        this._emit('error', err);
      };
    } catch (e) {
      console.error('[WebSocket] Connection failed:', e);
      this._scheduleReconnect(path);
    }
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close(1000, 'Client disconnected');
      this.ws = null;
    }
  }

  send(data) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(typeof data === 'string' ? data : JSON.stringify(data));
      return true;
    }
    return false;
  }

  on(event, callback) {
    if (!this.listeners.has(event)) this.listeners.set(event, []);
    this.listeners.get(event).push(callback);
    return () => this.off(event, callback);
  }

  off(event, callback) {
    const cbs = this.listeners.get(event);
    if (cbs) this.listeners.set(event, cbs.filter(cb => cb !== callback));
  }

  _emit(event, data) {
    const cbs = this.listeners.get(event);
    if (cbs) cbs.forEach(cb => cb(data));
  }

  _scheduleReconnect(path) {
    if (this.reconnectTimer) return;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      console.log('[WebSocket] Reconnecting...');
      this.connect(path);
    }, 3000);
  }
}

export const chatSocket = new ChatSocket();

export function connectNotificationSocket() {
  const notifSocket = new ChatSocket();
  notifSocket.connect('/notifications');
  notifSocket.on('message', (data) => {
    toast(data.title || data.message || 'New notification', 'info');
  });
  return notifSocket;
}
