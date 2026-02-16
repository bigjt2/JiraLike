import React, { useState } from 'react';
import { ticketsApi } from '../../services/api';
import './NewTicketModal.css';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const TICKET_TYPES = ['STORY', 'BUG', 'TASK', 'EPIC', 'SUBTASK'];

function NewTicketModal({ projectId, columnId, columns, users, onClose, onCreated }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    priority: 'MEDIUM',
    ticketType: 'TASK',
    storyPoints: '',
    dueDate: '',
    columnId: columnId || (columns[0]?.id ?? ''),
    assigneeId: '',
    reporterId: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) {
      setError('Title is required');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const ticket = await ticketsApi.create({
        ...form,
        projectId,
        columnId: Number(form.columnId),
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        reporterId: form.reporterId ? Number(form.reporterId) : null,
        storyPoints: form.storyPoints ? Number(form.storyPoints) : null,
        dueDate: form.dueDate || null,
      });
      onCreated(ticket);
    } catch (err) {
      setError('Failed to create ticket');
    } finally {
      setSaving(false);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="new-ticket-modal">
        <div className="modal-header">
          <h2>Create Ticket</h2>
          <button className="btn-icon-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="form-error">{error}</div>}

          <div className="form-group">
            <label>Title *</label>
            <input
              autoFocus
              type="text"
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              placeholder="Brief summary of the ticket"
              className="form-input"
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Type</label>
              <select
                value={form.ticketType}
                onChange={e => setForm(f => ({ ...f, ticketType: e.target.value }))}
                className="form-select"
              >
                {TICKET_TYPES.map(t => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Priority</label>
              <select
                value={form.priority}
                onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}
                className="form-select"
              >
                {PRIORITIES.map(p => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Column</label>
            <select
              value={form.columnId}
              onChange={e => setForm(f => ({ ...f, columnId: e.target.value }))}
              className="form-select"
            >
              {columns.map(col => (
                <option key={col.id} value={col.id}>{col.name}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="Detailed description..."
              className="form-textarea"
              rows={3}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Assignee</label>
              <select
                value={form.assigneeId}
                onChange={e => setForm(f => ({ ...f, assigneeId: e.target.value }))}
                className="form-select"
              >
                <option value="">Unassigned</option>
                {users.map(u => (
                  <option key={u.id} value={u.id}>{u.displayName}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Reporter</label>
              <select
                value={form.reporterId}
                onChange={e => setForm(f => ({ ...f, reporterId: e.target.value }))}
                className="form-select"
              >
                <option value="">None</option>
                {users.map(u => (
                  <option key={u.id} value={u.id}>{u.displayName}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Story Points</label>
              <input
                type="number"
                min="0"
                max="100"
                value={form.storyPoints}
                onChange={e => setForm(f => ({ ...f, storyPoints: e.target.value }))}
                placeholder="—"
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label>Due Date</label>
              <input
                type="date"
                value={form.dueDate}
                onChange={e => setForm(f => ({ ...f, dueDate: e.target.value }))}
                className="form-input"
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn-outline" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Creating...' : 'Create Ticket'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default NewTicketModal;
