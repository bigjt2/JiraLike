import React, { useState } from 'react';
import { useDroppable } from '@dnd-kit/core';
import TicketCard from '../TicketCard/TicketCard';
import './KanbanColumn.css';

function KanbanColumn({ column, onTicketClick, onAddTicket, onDeleteColumn }) {
  const [showMenu, setShowMenu] = useState(false);

  const { setNodeRef, isOver } = useDroppable({ id: column.id });

  const tickets = column.tickets || [];
  const ticketCount = tickets.length;

  const colorStyle = column.color
    ? { borderTopColor: column.color }
    : {};

  return (
    <div className={`kanban-column ${isOver ? 'is-over' : ''}`} style={colorStyle}>
      <div className="column-header">
        <div className="column-header-left">
          <span className="column-color-dot" style={{ background: column.color || '#64748b' }} />
          <h3 className="column-name">{column.name}</h3>
          <span className="ticket-count">{ticketCount}</span>
        </div>
        <div className="column-actions">
          <button
            className="btn-icon-sm"
            onClick={() => onAddTicket(column.id)}
            title="Add ticket"
          >
            +
          </button>
          <div className="column-menu-wrapper">
            <button
              className="btn-icon-sm"
              onClick={() => setShowMenu(m => !m)}
              title="Column options"
            >
              ⋯
            </button>
            {showMenu && (
              <div className="column-menu">
                <button
                  className="column-menu-item danger"
                  onClick={() => {
                    setShowMenu(false);
                    onDeleteColumn(column.id);
                  }}
                >
                  Delete column
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="column-tickets" ref={setNodeRef}>
        {tickets.map(ticket => (
          <TicketCard
            key={ticket.id}
            ticket={ticket}
            onClick={() => onTicketClick(ticket)}
          />
        ))}
        {tickets.length === 0 && (
          <div className="column-empty">
            <span>Drop tickets here</span>
          </div>
        )}
      </div>

      <button className="add-ticket-btn" onClick={() => onAddTicket(column.id)}>
        + Add ticket
      </button>
    </div>
  );
}

export default KanbanColumn;
