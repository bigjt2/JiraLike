import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Projects
export const projectsApi = {
  getAll: () => api.get('/projects').then(r => r.data),
  getById: (id) => api.get(`/projects/${id}`).then(r => r.data),
  getByKey: (key) => api.get(`/projects/key/${key}`).then(r => r.data),
  create: (data) => api.post('/projects', data).then(r => r.data),
  update: (id, data) => api.put(`/projects/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/projects/${id}`),
};

// Board Columns
export const columnsApi = {
  getByProject: (projectId) => api.get(`/projects/${projectId}/columns`).then(r => r.data),
  create: (data) => api.post('/columns', data).then(r => r.data),
  update: (id, data) => api.put(`/columns/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/columns/${id}`),
};

// Tickets
export const ticketsApi = {
  getByProject: (projectId) => api.get(`/projects/${projectId}/tickets`).then(r => r.data),
  getById: (id) => api.get(`/tickets/${id}`).then(r => r.data),
  create: (data) => api.post('/tickets', data).then(r => r.data),
  update: (id, data) => api.put(`/tickets/${id}`, data).then(r => r.data),
  move: (id, data) => api.patch(`/tickets/${id}/move`, data).then(r => r.data),
  delete: (id) => api.delete(`/tickets/${id}`),
};

// Users
export const usersApi = {
  getAll: () => api.get('/users').then(r => r.data),
  getById: (id) => api.get(`/users/${id}`).then(r => r.data),
  create: (data) => api.post('/users', data).then(r => r.data),
  update: (id, data) => api.put(`/users/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/users/${id}`),
};

// Comments
export const commentsApi = {
  getByTicket: (ticketId) => api.get(`/tickets/${ticketId}/comments`).then(r => r.data),
  create: (ticketId, data) => api.post(`/tickets/${ticketId}/comments`, data).then(r => r.data),
  update: (id, data) => api.put(`/comments/${id}`, data).then(r => r.data),
  delete: (id) => api.delete(`/comments/${id}`),
};

export default api;
