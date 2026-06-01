import { Session, toast } from './utils.js';

const WS_BASE = window.location.hostname.includes('vercel.app')
  ? 'wss://oop-project-production.up.railway.app/ws'
  : 'ws://localhost:8080/ws';

// ═══════════════════════════════════════════════════════════════
// OOP — CLASS: ChatSocket (ENCAPSULATION via constructor closure)
// OOP — ABSTRACTION: Hides WebSocket raw API behind simple methods
// ═══════════════════════════════════════════════════════════════
class ChatSocket {
  // ENCAPSULATION — internal state hidden from outside world
  constructor() {
    this.ws = null;              // private: WebSocket connection
    this.reconnectTimer = null;  // private: reconnect timeout handle
    this.listeners = new Map();  // private: event → callbacks
  }

  // ABSTRACTION — wraps raw WebSocket connect + token injection
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

  // ABSTRACTION — cleanly tears down connection and timers
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

  // ABSTRACTION — sends data, auto-serializes objects
  send(data) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(typeof data === 'string' ? data : JSON.stringify(data));
      return true;
    }
    return false;
  }

  // POLYMORPHISM — event-name-driven dispatch (same interface, different events)
  on(event, callback) {
    if (!this.listeners.has(event)) this.listeners.set(event, []);
    this.listeners.get(event).push(callback);
    return () => this.off(event, callback);
  }

  // COMPOSITION — holds a Map of callback arrays; removing from composition
  off(event, callback) {
    const cbs = this.listeners.get(event);
    if (cbs) this.listeners.set(event, cbs.filter(cb => cb !== callback));
  }

  // ABSTRACTION — internal dispatch abstracts iteration over listeners
  _emit(event, data) {
    const cbs = this.listeners.get(event);
    if (cbs) cbs.forEach(cb => cb(data));
  }

  // ENCAPSULATION — auto-reconnect logic hidden inside the class
  _scheduleReconnect(path) {
    if (this.reconnectTimer) return;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      console.log('[WebSocket] Reconnecting...');
      this.connect(path);
    }, 3000);
  }
}

// OOP — INSTANTIATION: Singleton instance of ChatSocket
export const chatSocket = new ChatSocket();

// OOP — INSTANTIATION: New instance every call (not singleton)
export function connectNotificationSocket() {
  const notifSocket = new ChatSocket(); // ⬅ new ChatSocket instance
  notifSocket.connect('/notifications');
  // POLYMORPHISM — same .on() interface, different callback behaviour
  notifSocket.on('message', (data) => {
    toast(data.title || data.message || 'New notification', 'info');
  });
  return notifSocket;
}
