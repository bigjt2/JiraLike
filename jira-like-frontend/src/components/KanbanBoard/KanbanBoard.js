import React, { useState } from 'react';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  PointerSensor,
  useSensor,
  useSensors,
  DragStartEvent,
  DragEndEvent,
  DragOverEvent,
} from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import KanbanColumn from './KanbanColumn';
import TicketCard from '../TicketCard/TicketCard';
import './KanbanBoard.css';

function KanbanBoard({ columns, onTicketMoved, onTicketClick, onAddTicket, onDeleteColumn }) {
  const [activeTicket, setActiveTicket] = useState(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: { distance: 5 },
    })
  );

  const findColumn = (ticketId) => {
    return columns.find(col => col.tickets?.some(t => t.id === ticketId));
  };

  const handleDragStart = (event) => {
    const { active } = event;
    const col = findColumn(active.id);
    if (col) {
      const ticket = col.tickets.find(t => t.id === active.id);
      setActiveTicket(ticket);
    }
  };

  const handleDragEnd = (event) => {
    const { active, over } = event;
    setActiveTicket(null);
    if (!over) return;

    const sourceCol = findColumn(active.id);
    if (!sourceCol) return;

    // Determine target column and position
    let targetColId;
    let targetPosition;

    // Check if dropped over a column container
    const targetCol = columns.find(c => c.id === over.id);
    if (targetCol) {
      targetColId = targetCol.id;
      targetPosition = targetCol.tickets?.length || 0;
    } else {
      // Dropped over another ticket
      const targetTicketCol = findColumn(over.id);
      if (!targetTicketCol) return;
      targetColId = targetTicketCol.id;
      const targetIdx = targetTicketCol.tickets.findIndex(t => t.id === over.id);
      targetPosition = targetIdx >= 0 ? targetIdx : targetTicketCol.tickets.length;
    }

    onTicketMoved(active.id, targetColId, targetPosition);
  };

  const handleDragOver = (event) => {
    // Visual feedback handled by column components
  };

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragOver={handleDragOver}
    >
      <div className="kanban-board">
        {columns.map(column => (
          <SortableContext
            key={column.id}
            items={(column.tickets || []).map(t => t.id)}
            strategy={verticalListSortingStrategy}
          >
            <KanbanColumn
              column={column}
              onTicketClick={onTicketClick}
              onAddTicket={onAddTicket}
              onDeleteColumn={onDeleteColumn}
            />
          </SortableContext>
        ))}
      </div>

      <DragOverlay>
        {activeTicket && (
          <div className="drag-overlay">
            <TicketCard ticket={activeTicket} isDragging />
          </div>
        )}
      </DragOverlay>
    </DndContext>
  );
}

export default KanbanBoard;
