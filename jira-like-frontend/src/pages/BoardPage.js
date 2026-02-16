import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { projectsApi, columnsApi, ticketsApi, usersApi } from '../services/api';
import KanbanBoard from '../components/KanbanBoard/KanbanBoard';
import TicketModal from '../components/TicketModal/TicketModal';
import NewTicketModal from '../components/NewTicketModal/NewTicketModal';
import './BoardPage.css';

function BoardPage() {
  const { projectId } = useParams();
  const navigate = useNavigate();

  const [project, setProject] = useState(null);
  const [columns, setColumns] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [showNewTicket, setShowNewTicket] = useState(false);
  const [newTicketColumnId, setNewTicketColumnId] = useState(null);

  const loadBoard = useCallback(async () => {
    try {
      setLoading(true);
      const [proj, cols, userList] = await Promise.all([
        projectsApi.getById(projectId),
        columnsApi.getByProject(projectId),
        usersApi.getAll(),
      ]);
      setProject(proj);
      setColumns(cols);
      setUsers(userList);
    } catch (err) {
      setError('Failed to load board');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    loadBoard();
  }, [loadBoard]);

  const handleTicketMoved = async (ticketId, targetColumnId, targetPosition) => {
    // Optimistically update local state
    setColumns(prev => {
      const updated = prev.map(col => ({
        ...col,
        tickets: col.tickets.filter(t => t.id !== ticketId),
      }));
      const ticket = prev.flatMap(c => c.tickets).find(t => t.id === ticketId);
      if (!ticket) return prev;
      return updated.map(col => {
        if (col.id === targetColumnId) {
          const newTickets = [...col.tickets];
          newTickets.splice(targetPosition, 0, { ...ticket, columnId: targetColumnId });
          return { ...col, tickets: newTickets };
        }
        return col;
      });
    });

    try {
      await ticketsApi.move(ticketId, { columnId: targetColumnId, position: targetPosition });
    } catch {
      // Revert on failure
      loadBoard();
    }
  };

  const handleTicketCreated = (ticket) => {
    setColumns(prev => prev.map(col =>
      col.id === ticket.columnId
        ? { ...col, tickets: [...col.tickets, ticket] }
        : col
    ));
    setShowNewTicket(false);
  };

  const handleTicketUpdated = (updatedTicket) => {
    setColumns(prev => prev.map(col => ({
      ...col,
      tickets: col.tickets.map(t => t.id === updatedTicket.id ? updatedTicket : t),
    })));
    setSelectedTicket(updatedTicket);
  };

  const handleTicketDeleted = (ticketId) => {
    setColumns(prev => prev.map(col => ({
      ...col,
      tickets: col.tickets.filter(t => t.id !== ticketId),
    })));
    setSelectedTicket(null);
  };

  const handleAddColumn = async () => {
    const name = window.prompt('Column name:');
    if (!name?.trim()) return;
    try {
      const col = await columnsApi.create({ name: name.trim(), projectId: Number(projectId) });
      setColumns(prev => [...prev, { ...col, tickets: [] }]);
    } catch {
      alert('Failed to create column');
    }
  };

  const handleDeleteColumn = async (columnId) => {
    if (!window.confirm('Delete this column? All tickets will be lost.')) return;
    try {
      await columnsApi.delete(columnId);
      setColumns(prev => prev.filter(c => c.id !== columnId));
    } catch {
      alert('Failed to delete column');
    }
  };

  if (loading) {
    return <div className="board-loading">Loading board...</div>;
  }

  if (error) {
    return (
      <div className="board-error">
        <p>{error}</p>
        <button onClick={() => navigate('/projects')}>Back to Projects</button>
      </div>
    );
  }

  return (
    <div className="board-page">
      <header className="board-header">
        <div className="board-header-left">
          <button className="btn-back" onClick={() => navigate('/projects')}>
            ← Projects
          </button>
          <div className="board-breadcrumb">
            <span className="project-key">{project?.key}</span>
            <span className="breadcrumb-sep">/</span>
            <span className="project-name">{project?.name}</span>
          </div>
        </div>
        <div className="board-header-right">
          <div className="user-avatars">
            {users.slice(0, 5).map(u => (
              <div key={u.id} className="user-avatar-sm" title={u.displayName}>
                {u.displayName?.charAt(0).toUpperCase()}
              </div>
            ))}
          </div>
          <button className="btn-primary" onClick={() => {
            setNewTicketColumnId(columns[0]?.id || null);
            setShowNewTicket(true);
          }}>
            + Create Ticket
          </button>
        </div>
      </header>

      <div className="board-toolbar">
        <h2 className="board-title">Kanban Board</h2>
        <button className="btn-ghost" onClick={handleAddColumn}>
          + Add Column
        </button>
      </div>

      <KanbanBoard
        columns={columns}
        onTicketMoved={handleTicketMoved}
        onTicketClick={setSelectedTicket}
        onAddTicket={(columnId) => {
          setNewTicketColumnId(columnId);
          setShowNewTicket(true);
        }}
        onDeleteColumn={handleDeleteColumn}
      />

      {selectedTicket && (
        <TicketModal
          ticket={selectedTicket}
          users={users}
          columns={columns}
          onClose={() => setSelectedTicket(null)}
          onUpdated={handleTicketUpdated}
          onDeleted={handleTicketDeleted}
        />
      )}

      {showNewTicket && (
        <NewTicketModal
          projectId={Number(projectId)}
          columnId={newTicketColumnId}
          columns={columns}
          users={users}
          onClose={() => setShowNewTicket(false)}
          onCreated={handleTicketCreated}
        />
      )}
    </div>
  );
}

export default BoardPage;
