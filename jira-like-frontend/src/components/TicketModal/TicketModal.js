import React, { useState, useEffect } from 'react';
import { ticketsApi, commentsApi } from '../../services/api';
import './TicketModal.css';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const TICKET_TYPES = ['STORY', 'BUG', 'TASK', 'EPIC', 'SUBTASK'];

const PRIORITY_COLORS = {
  LOW: '#10b981', MEDIUM: '#f59e0b', HIGH: '#f97316', CRITICAL: '#ef4444',
};

const TYPE_ICONS = {
  STORY: '📗', BUG: '🐛', TASK: '✓', EPIC: '⚡', SUBTASK: '↳',
};

function TicketModal({ ticket, users, columns, onClose, onUpdated, onDeleted }) {
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({
    title: ticket.title || '',
    description: ticket.description || '',
    priority: ticket.priority || 'MEDIUM',
    ticketType: ticket.ticketType || 'TASK',
    storyPoints: ticket.storyPoints || '',
    dueDate: ticket.dueDate || '',
    columnId: ticket.columnId || '',
    assigneeId: ticket.assignee?.id || '',
    reporterId: ticket.reporter?.id || '',
  });
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  const [commentAuthorId, setCommentAuthorId] = useState(users[0]?.id || '');
  const [saving, setSaving] = useState(false);
  const [activeTab, setActiveTab] = useState('details');

  useEffect(() => {
    loadComments();
  }, [ticket.id]);

  const loadComments = async () => {
    try {
      const data = await commentsApi.getByTicket(ticket.id);
      setComments(data);
    } catch {
      // silently ignore
    }
  };

  const handleSave = async () => {
    if (!form.title.trim()) return;
    setSaving(true);
    try {
      const updated = await ticketsApi.update(ticket.id, {
        ...form,
        projectId: ticket.projectId,
        columnId: Number(form.columnId),
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        reporterId: form.reporterId ? Number(form.reporterId) : null,
        storyPoints: form.storyPoints ? Number(form.storyPoints) : null,
        dueDate: form.dueDate || null,
      });
      onUpdated(updated);
      setEditing(false);
    } catch (err) {
      alert('Failed to save changes');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this ticket?')) return;
    try {
      await ticketsApi.delete(ticket.id);
      onDeleted(ticket.id);
    } catch {
      alert('Failed to delete ticket');
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim() || !commentAuthorId) return;
    try {
      const comment = await commentsApi.create(ticket.id, {
        content: newComment.trim(),
        authorId: Number(commentAuthorId),
      });
      setComments(prev => [...prev, comment]);
      setNewComment('');
    } catch {
      alert('Failed to add comment');
    }
  };

  const handleDeleteComment = async (commentId) => {
    try {
      await commentsApi.delete(commentId);
      setComments(prev => prev.filter(c => c.id !== commentId));
    } catch {
      alert('Failed to delete comment');
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="ticket-modal">
        <div className="ticket-modal-header">
          <div className="ticket-modal-title-row">
            <span className="ticket-modal-id">
              {TYPE_ICONS[ticket.ticketType]} {ticket.projectKey}-{ticket.id}
            </span>
            <div className="ticket-modal-actions">
              {!editing && (
                <button className="btn-sm btn-outline" onClick={() => setEditing(true)}>
                  Edit
                </button>
              )}
              <button className="btn-sm btn-danger" onClick={handleDelete}>
                Delete
              </button>
              <button className="btn-icon-close" onClick={onClose}>✕</button>
            </div>
          </div>
          {editing ? (
            <input
              className="ticket-title-input"
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              placeholder="Ticket title"
            />
          ) : (
            <h2 className="ticket-modal-title">{ticket.title}</h2>
          )}
        </div>

        <div className="ticket-modal-tabs">
          <button
            className={`tab ${activeTab === 'details' ? 'active' : ''}`}
            onClick={() => setActiveTab('details')}
          >
            Details
          </button>
          <button
            className={`tab ${activeTab === 'comments' ? 'active' : ''}`}
            onClick={() => setActiveTab('comments')}
          >
            Comments {comments.length > 0 && `(${comments.length})`}
          </button>
        </div>

        <div className="ticket-modal-body">
          {activeTab === 'details' && (
            <div className="ticket-details">
              <div className="ticket-detail-grid">
                <div className="detail-row">
                  <label>Status</label>
                  {editing ? (
                    <select
                      value={form.columnId}
                      onChange={e => setForm(f => ({ ...f, columnId: e.target.value }))}
                    >
                      {columns.map(col => (
                        <option key={col.id} value={col.id}>{col.name}</option>
                      ))}
                    </select>
                  ) : (
                    <span className="detail-value status-badge">{ticket.columnName}</span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Priority</label>
                  {editing ? (
                    <select
                      value={form.priority}
                      onChange={e => setForm(f => ({ ...f, priority: e.target.value }))}
                    >
                      {PRIORITIES.map(p => (
                        <option key={p} value={p}>{p}</option>
                      ))}
                    </select>
                  ) : (
                    <span
                      className="detail-value priority-badge"
                      style={{ color: PRIORITY_COLORS[ticket.priority] }}
                    >
                      {ticket.priority}
                    </span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Type</label>
                  {editing ? (
                    <select
                      value={form.ticketType}
                      onChange={e => setForm(f => ({ ...f, ticketType: e.target.value }))}
                    >
                      {TICKET_TYPES.map(t => (
                        <option key={t} value={t}>{t}</option>
                      ))}
                    </select>
                  ) : (
                    <span className="detail-value">
                      {TYPE_ICONS[ticket.ticketType]} {ticket.ticketType}
                    </span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Assignee</label>
                  {editing ? (
                    <select
                      value={form.assigneeId}
                      onChange={e => setForm(f => ({ ...f, assigneeId: e.target.value }))}
                    >
                      <option value="">Unassigned</option>
                      {users.map(u => (
                        <option key={u.id} value={u.id}>{u.displayName}</option>
                      ))}
                    </select>
                  ) : (
                    <span className="detail-value">
                      {ticket.assignee ? (
                        <span className="user-chip">
                          <span className="user-chip-avatar">
                            {ticket.assignee.displayName?.charAt(0)}
                          </span>
                          {ticket.assignee.displayName}
                        </span>
                      ) : 'Unassigned'}
                    </span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Reporter</label>
                  {editing ? (
                    <select
                      value={form.reporterId}
                      onChange={e => setForm(f => ({ ...f, reporterId: e.target.value }))}
                    >
                      <option value="">None</option>
                      {users.map(u => (
                        <option key={u.id} value={u.id}>{u.displayName}</option>
                      ))}
                    </select>
                  ) : (
                    <span className="detail-value">
                      {ticket.reporter ? (
                        <span className="user-chip">
                          <span className="user-chip-avatar">
                            {ticket.reporter.displayName?.charAt(0)}
                          </span>
                          {ticket.reporter.displayName}
                        </span>
                      ) : 'None'}
                    </span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Story Points</label>
                  {editing ? (
                    <input
                      type="number"
                      min="0"
                      max="100"
                      value={form.storyPoints}
                      onChange={e => setForm(f => ({ ...f, storyPoints: e.target.value }))}
                      placeholder="—"
                    />
                  ) : (
                    <span className="detail-value">
                      {ticket.storyPoints != null ? ticket.storyPoints : '—'}
                    </span>
                  )}
                </div>

                <div className="detail-row">
                  <label>Due Date</label>
                  {editing ? (
                    <input
                      type="date"
                      value={form.dueDate}
                      onChange={e => setForm(f => ({ ...f, dueDate: e.target.value }))}
                    />
                  ) : (
                    <span className="detail-value">
                      {ticket.dueDate ? new Date(ticket.dueDate).toLocaleDateString() : '—'}
                    </span>
                  )}
                </div>
              </div>

              <div className="ticket-description-section">
                <label>Description</label>
                {editing ? (
                  <textarea
                    className="description-editor"
                    value={form.description}
                    onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                    placeholder="Add a description..."
                    rows={5}
                  />
                ) : (
                  <div className="description-text">
                    {ticket.description || <span className="empty-desc">No description</span>}
                  </div>
                )}
              </div>

              {editing && (
                <div className="edit-actions">
                  <button className="btn-primary" onClick={handleSave} disabled={saving}>
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button className="btn-outline" onClick={() => setEditing(false)}>
                    Cancel
                  </button>
                </div>
              )}
            </div>
          )}

          {activeTab === 'comments' && (
            <div className="ticket-comments">
              <div className="comments-list">
                {comments.length === 0 && (
                  <p className="no-comments">No comments yet</p>
                )}
                {comments.map(comment => (
                  <div key={comment.id} className="comment">
                    <div className="comment-header">
                      <span className="comment-author">
                        <span className="user-chip-avatar">
                          {comment.author?.displayName?.charAt(0)}
                        </span>
                        {comment.author?.displayName}
                      </span>
                      <span className="comment-date">
                        {new Date(comment.createdAt).toLocaleDateString()}
                      </span>
                      <button
                        className="btn-icon-xs"
                        onClick={() => handleDeleteComment(comment.id)}
                      >
                        ✕
                      </button>
                    </div>
                    <p className="comment-content">{comment.content}</p>
                  </div>
                ))}
              </div>

              <div className="add-comment">
                <select
                  className="comment-author-select"
                  value={commentAuthorId}
                  onChange={e => setCommentAuthorId(e.target.value)}
                >
                  <option value="">Select author...</option>
                  {users.map(u => (
                    <option key={u.id} value={u.id}>{u.displayName}</option>
                  ))}
                </select>
                <textarea
                  className="comment-input"
                  value={newComment}
                  onChange={e => setNewComment(e.target.value)}
                  placeholder="Write a comment..."
                  rows={3}
                  onKeyDown={e => {
                    if (e.key === 'Enter' && e.ctrlKey) handleAddComment();
                  }}
                />
                <button
                  className="btn-primary"
                  onClick={handleAddComment}
                  disabled={!newComment.trim() || !commentAuthorId}
                >
                  Add Comment
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default TicketModal;
