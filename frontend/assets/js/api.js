/* ============================================================
   PMS — Centralized Axios API Manager
   api.js | Base URL: http://localhost:8080/api/v1
   ============================================================ */
'use strict';

import { Session, toast } from './utils.js';

const RAILWAY_URL = 'https://oop-project-production.up.railway.app/api/v1';
const BASE_URL = (typeof API_URL !== 'undefined' ? API_URL : RAILWAY_URL);

// ─── Create Axios Instance ───────────────────────────────
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// ─── Request Interceptor — Attach Bearer Token ───────────
api.interceptors.request.use(
  config => {
    const token = Session.getToken();
    if (token) config.headers['Authorization'] = `Bearer ${token}`;
    return config;
  },
  err => Promise.reject(err)
);

// ─── Response Interceptor — Handle Errors & Token Refresh ─
let isRefreshing = false;
let failedQueue  = [];

function processQueue(error, token = null) {
  failedQueue.forEach(prom => error ? prom.reject(error) : prom.resolve(token));
  failedQueue = [];
}

api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config;
    if (err.response?.status === 401 && !original._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          original.headers['Authorization'] = `Bearer ${token}`;
          return api(original);
        }).catch(e => Promise.reject(e));
      }
      original._retry = true;
      isRefreshing = true;
      const refreshToken = Session.getRefresh();
      if (refreshToken) {
        try {
          const res = await axios.post(`${BASE_URL}/auth/refresh-token`, null, { params: { refreshToken } });
          const { token, refreshToken: newRefresh } = res.data.data;
          const user = Session.getUser();
          Session.set(token, newRefresh, user);
          api.defaults.headers['Authorization'] = `Bearer ${token}`;
          processQueue(null, token);
          isRefreshing = false;
          original.headers['Authorization'] = `Bearer ${token}`;
          return api(original);
        } catch (refreshErr) {
          processQueue(refreshErr, null);
          isRefreshing = false;
          Session.clear();
          toast('Session expired. Please log in again.', 'error');
          setTimeout(() => window.location.href = '/pages/login.html', 1500);
          return Promise.reject(refreshErr);
        }
      } else {
        Session.clear();
        window.location.href = '/pages/login.html';
      }
    }

    // Generic error messages
    const status = err.response?.status;
    const message = err.response?.data?.message || err.message || 'An unexpected error occurred.';
    if (status === 403)       toast('Access denied. You do not have permission.', 'error');
    else if (status === 404)  toast('Resource not found.', 'error');
    else if (status === 409)  toast(message, 'warning');
    else if (status === 422)  toast(message, 'warning');
    else if (status >= 500)   toast('Server error. Please try again later.', 'error');

    return Promise.reject(err);
  }
);

// ─── Auth Endpoints ──────────────────────────────────────
export const AuthAPI = {
  login(data)              { return api.post('/auth/login', data); },
  register(data)           { return api.post('/auth/register', data); },
  forgotPassword(email)    { return api.post('/auth/forgot-password', { email }); },
  resetPassword(data)      { return api.post('/auth/reset-password', data); },
  refreshToken(token)      { return api.post('/auth/refresh-token', null, { params: { refreshToken: token } }); },
  verifyEmail(token)       { return api.post('/auth/verify-email', null, { params: { token } }); },
};

// ─── User Endpoints ──────────────────────────────────────
export const UserAPI = {
  getAll(page = 0, size = 20)       { return api.get('/users', { params: { page, size } }); },
  getById(id)                        { return api.get(`/users/${id}`); },
  updateUser(id, data) {
    return api.put(`/users/${id}`, data);
  },
  changePassword(id, data) {
    return api.post(`/users/${id}/change-password`, data);
  },
  updateRole(id, data) {
    return api.patch(`/users/${id}/role`, data);
  },
};

// ─── Project Endpoints ───────────────────────────────────
export const ProjectAPI = {
  create(data)             { return api.post('/projects', data); },
  getAll(page = 0, size = 10) { return api.get('/projects', { params: { page, size } }); },
  getById(id)              { return api.get(`/projects/${id}`); },
  update(id, data)         { return api.put(`/projects/${id}`, data); },
  delete(id)               { return api.delete(`/projects/${id}`); },
  updateStatus(id, status) { return api.patch(`/projects/${id}/status`, null, { params: { status } }); },
  calculateProgress(id)    { return api.post(`/projects/${id}/calculate-progress`); },
};

// ─── Task Endpoints ──────────────────────────────────────
export const TaskAPI = {
  create(data)                    { return api.post('/tasks', data); },
  getAll(page = 0, size = 20)     { return api.get('/tasks', { params: { page, size } }); },
  getById(id)                     { return api.get(`/tasks/${id}`); },
  update(id, data)                { return api.put(`/tasks/${id}`, data); },
  delete(id)                      { return api.delete(`/tasks/${id}`); },
  updateStatus(id, status)        { return api.patch(`/tasks/${id}/status`, null, { params: { status } }); },
  assign(id, assignedToId)        { return api.patch(`/tasks/${id}/assign`, null, { params: { assignedToId } }); },
  getByProject(projectId, page = 0, size = 50) {
    return api.get(`/tasks/project/${projectId}`, { params: { page, size } });
  },
  getByUser(userId, page = 0, size = 20) {
    return api.get(`/tasks/assigned/${userId}`, { params: { page, size } });
  },
  getOverdue()                    { return api.get('/tasks/overdue'); },
};

// ─── Comment Endpoints ───────────────────────────────────
export const CommentAPI = {
  create(taskId, message)    { return api.post('/comments', { taskId, message }); },
  getByTask(taskId, page = 0){ return api.get(`/comments/task/${taskId}`, { params: { page, size: 20 } }); },
  delete(id)                 { return api.delete(`/comments/${id}`); },
};

// ─── Team Endpoints ──────────────────────────────────────
export const TeamAPI = {
  create(data)               { return api.post('/teams', data); },
  getAll(page = 0, size = 10){ return api.get('/teams', { params: { page, size } }); },
  getById(id)                { return api.get(`/teams/${id}`); },
  update(id, data)           { return api.put(`/teams/${id}`, data); },
  delete(id)                 { return api.delete(`/teams/${id}`); },
  addMember(teamId, userId, role) {
    return api.post(`/teams/${teamId}/members`, null, { params: { userId, role } });
  },
  removeMember(teamId, userId) { return api.delete(`/teams/${teamId}/members/${userId}`); },
};

// ─── Dashboard Endpoints ─────────────────────────────────
export const DashboardAPI = {
  getStats()     { return api.get('/dashboard/stats'); },
  getAnalytics() { return api.get('/dashboard/analytics'); },
};

// ─── Message Endpoints ───────────────────────────────────
export const MessageAPI = {
  send(receiverId, content) {
    return api.post('/messages', { receiverId, content });
  },
  getChatHistory(otherUserId, page = 0, size = 20) {
    return api.get(`/messages/chat/${otherUserId}`, { params: { page, size } });
  },
  markAsRead(senderId) { return api.post(`/messages/chat/${senderId}/read`); },
};

// ─── Notification Endpoints ──────────────────────────────
export const NotificationAPI = {
  getAll(page = 0, size = 10) { return api.get('/notifications', { params: { page, size } }); },
  markRead(id)                 { return api.patch(`/notifications/${id}/read`); },
  markAllRead()                { return api.post('/notifications/read-all'); },
};

// ─── File Endpoints ──────────────────────────────────────
export const FileAPI = {
  upload(projectId, file, onProgress) {
    const fd = new FormData();
    fd.append('file', file);
    return api.post('/files/upload', fd, {
      params: { projectId },
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: e => onProgress && onProgress(Math.round((e.loaded * 100) / e.total)),
    });
  },
  download(id) { return `${BASE_URL}/files/download/${id}`; },
  delete(id)   { return api.delete(`/files/${id}`); },
  getByProject(projectId, page = 0) {
    return api.get(`/files/project/${projectId}`, { params: { page, size: 20 } });
  },
};

// ─── Report Endpoints ────────────────────────────────────
export const ReportAPI = {
  generate(projectId, reportType) {
    return api.post('/reports/generate', null, { params: { projectId, reportType } });
  },
  download(id) { return `${BASE_URL}/reports/download/${id}`; },
  getByProject(projectId, page = 0) {
    return api.get(`/reports/project/${projectId}`, { params: { page, size: 10 } });
  },
};

// ─── Milestone Endpoints ─────────────────────────────────
export const MilestoneAPI = {
  create(data)         { return api.post('/milestones', data); },
  getByProject(id)     { return api.get(`/milestones/project/${id}`); },
  update(id, data)     { return api.put(`/milestones/${id}`, data); },
  delete(id)           { return api.delete(`/milestones/${id}`); },
  complete(id)         { return api.patch(`/milestones/${id}/complete`); },
};

export default api;
