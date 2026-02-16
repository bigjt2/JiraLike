package com.jiralike.service;

import com.jiralike.dto.*;
import com.jiralike.entity.*;
import com.jiralike.repository.BoardColumnRepository;
import com.jiralike.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BoardColumnRepository columnRepository;
    private final ProjectService projectService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<TicketDto> findByProject(Long projectId) {
        return ticketRepository.findByProjectIdWithUsers(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketDto findById(Long id) {
        return toDto(getTicketOrThrow(id));
    }

    @Transactional
    public TicketDto create(TicketCreateDto dto) {
        Project project = projectService.getProjectOrThrow(dto.getProjectId());
        BoardColumn column = getColumnOrThrow(dto.getColumnId());

        Integer maxPos = ticketRepository.findMaxPositionInColumn(column.getId());
        int position = (maxPos == null) ? 0 : maxPos + 1;

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setPriority(dto.getPriority() != null ? dto.getPriority() : Ticket.Priority.MEDIUM);
        ticket.setTicketType(dto.getTicketType() != null ? dto.getTicketType() : Ticket.TicketType.TASK);
        ticket.setStoryPoints(dto.getStoryPoints());
        ticket.setDueDate(dto.getDueDate());
        ticket.setProject(project);
        ticket.setColumn(column);
        ticket.setPosition(position);

        if (dto.getAssigneeId() != null) {
            ticket.setAssignee(userService.getUserOrThrow(dto.getAssigneeId()));
        }
        if (dto.getReporterId() != null) {
            ticket.setReporter(userService.getUserOrThrow(dto.getReporterId()));
        }

        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto update(Long id, TicketCreateDto dto) {
        Ticket ticket = getTicketOrThrow(id);

        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        if (dto.getPriority() != null) ticket.setPriority(dto.getPriority());
        if (dto.getTicketType() != null) ticket.setTicketType(dto.getTicketType());
        ticket.setStoryPoints(dto.getStoryPoints());
        ticket.setDueDate(dto.getDueDate());

        if (dto.getColumnId() != null && !dto.getColumnId().equals(ticket.getColumn().getId())) {
            ticket.setColumn(getColumnOrThrow(dto.getColumnId()));
        }
        if (dto.getAssigneeId() != null) {
            ticket.setAssignee(userService.getUserOrThrow(dto.getAssigneeId()));
        } else {
            ticket.setAssignee(null);
        }
        if (dto.getReporterId() != null) {
            ticket.setReporter(userService.getUserOrThrow(dto.getReporterId()));
        }

        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto move(Long id, TicketMoveDto dto) {
        Ticket ticket = getTicketOrThrow(id);
        BoardColumn targetColumn = getColumnOrThrow(dto.getColumnId());

        // Shift positions of tickets in target column at or after the target position
        List<Ticket> ticketsInColumn = ticketRepository.findByColumnIdOrderByPositionAsc(targetColumn.getId());
        ticketsInColumn.stream()
                .filter(t -> !t.getId().equals(id) && t.getPosition() >= dto.getPosition())
                .forEach(t -> {
                    t.setPosition(t.getPosition() + 1);
                    ticketRepository.save(t);
                });

        ticket.setColumn(targetColumn);
        ticket.setPosition(dto.getPosition());
        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(Long id) {
        getTicketOrThrow(id);
        ticketRepository.deleteById(id);
    }

    private Ticket getTicketOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    private BoardColumn getColumnOrThrow(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found: " + id));
    }

    public TicketDto toDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setTicketType(ticket.getTicketType());
        dto.setPosition(ticket.getPosition());
        dto.setStoryPoints(ticket.getStoryPoints());
        dto.setDueDate(ticket.getDueDate());
        dto.setProjectId(ticket.getProject().getId());
        dto.setProjectKey(ticket.getProject().getKey());
        dto.setColumnId(ticket.getColumn().getId());
        dto.setColumnName(ticket.getColumn().getName());
        dto.setAssignee(userService.toDto(ticket.getAssignee()));
        dto.setReporter(userService.toDto(ticket.getReporter()));
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        return dto;
    }
}
