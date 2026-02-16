import React, { useState } from 'react';
import { projectsApi } from '../../services/api';
import './NewProjectModal.css';

function NewProjectModal({ onClose, onCreated }) {
  const [form, setForm] = useState({
    name: '',
    key: '',
    description: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleNameChange = (e) => {
    const name = e.target.value;
    const key = name
      .toUpperCase()
      .replace(/[^A-Z0-9]/g, '')
      .slice(0, 8);
    setForm(f => ({ ...f, name, key }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) { setError('Name is required'); return; }
    if (!form.key.trim()) { setError('Key is required'); return; }
    if (!/^[A-Z0-9]+$/.test(form.key)) { setError('Key must be uppercase letters and numbers only'); return; }

    setSaving(true);
    setError('');
    try {
      const project = await projectsApi.create(form);
      onCreated(project);
    } catch (err) {
      if (err.response?.status === 409) {
        setError('Project key already exists');
      } else {
        setError('Failed to create project');
      }
    } finally {
      setSaving(false);
    }
  };

  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick}>
      <div className="new-project-modal">
        <div className="modal-header">
          <h2>Create Project</h2>
          <button className="btn-icon-close" onClick={onClose}>✕</button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {error && <div className="form-error">{error}</div>}

          <div className="form-group">
            <label>Project Name *</label>
            <input
              autoFocus
              type="text"
              value={form.name}
              onChange={handleNameChange}
              placeholder="e.g. My Project"
              className="form-input"
              maxLength={100}
            />
          </div>

          <div className="form-group">
            <label>Project Key *</label>
            <input
              type="text"
              value={form.key}
              onChange={e => setForm(f => ({
                ...f,
                key: e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 10),
              }))}
              placeholder="e.g. PROJ"
              className="form-input key-input"
              maxLength={10}
            />
            <span className="form-hint">2–10 uppercase letters/numbers. Used as ticket prefix.</span>
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="What is this project about?"
              className="form-textarea"
              rows={3}
            />
          </div>

          <div className="form-note">
            <span>🗂</span>
            Default columns will be created: <strong>To Do → In Progress → In Review → Done</strong>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn-outline" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Creating...' : 'Create Project'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default NewProjectModal;
