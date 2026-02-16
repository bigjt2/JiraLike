import React from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import './TicketCard.css';

const PRIORITY_CONFIG = {
  LOW: { label: 'Low', color: '#10b981', icon: '↓' },
  MEDIUM: { label: 'Medium', color: '#f59e0b', icon: '→' },
  HIGH: { label: 'High', color: '#f97316', icon: '↑' },
  CRITICAL: { label: 'Critical', color: '#ef4444', icon: '⚡' },
};

const TYPE_CONFIG = {
  STORY: { label: 'Story', icon: '📗', color: '#10b981' },
  BUG: { label: 'Bug', icon: '🐛', color: '#ef4444' },
  TASK: { label: 'Task', icon: '✓', color: '#3b82f6' },
  EPIC: { label: 'Epic', icon: '⚡', color: '#8b5cf6' },
  SUBTASK: { label: 'Subtask', icon: '↳', color: '#64748b' },
};

function TicketCard({ ticket, onClick, isDragging }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging: isSortableDragging,
  } = useSortable({ id: ticket.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isSortableDragging ? 0.3 : 1,
  };

  const priority = PRIORITY_CONFIG[ticket.priority] || PRIORITY_CONFIG.MEDIUM;
  const type = TYPE_CONFIG[ticket.ticketType] || TYPE_CONFIG.TASK;

  const handleClick = (e) => {
    if (isDragging) return;
    onClick && onClick(ticket);
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`ticket-card ${isDragging ? 'is-dragging' : ''}`}
      onClick={handleClick}
      {...attributes}
      {...listeners}
    >
      <div className="ticket-card-top">
        <div className="ticket-type-badge" style={{ color: type.color }}>
          <span>{type.icon}</span>
          <span className="ticket-type-label">{type.label}</span>
        </div>
        <div className="ticket-priority" title={`Priority: ${priority.label}`}>
          <span style={{ color: priority.color }}>{priority.icon}</span>
        </div>
      </div>

      <h4 className="ticket-title">{ticket.title}</h4>

      {ticket.description && (
        <p className="ticket-description">{ticket.description}</p>
      )}

      <div className="ticket-card-footer">
        <div className="ticket-meta-left">
          {ticket.storyPoints != null && (
            <span className="story-points">{ticket.storyPoints} pts</span>
          )}
          {ticket.dueDate && (
            <span className={`due-date ${isOverdue(ticket.dueDate) ? 'overdue' : ''}`}>
              📅 {formatDate(ticket.dueDate)}
            </span>
          )}
        </div>
        {ticket.assignee && (
          <div className="ticket-assignee" title={ticket.assignee.displayName}>
            {ticket.assignee.displayName?.charAt(0).toUpperCase()}
          </div>
        )}
      </div>

      <div className="ticket-id-row">
        <span className="ticket-id">{ticket.projectKey}-{ticket.id}</span>
        {ticket.comments?.length > 0 && (
          <span className="comment-count">💬 {ticket.comments.length}</span>
        )}
      </div>
    </div>
  );
}

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function isOverdue(dateStr) {
  return new Date(dateStr) < new Date();
}

export default TicketCard;
