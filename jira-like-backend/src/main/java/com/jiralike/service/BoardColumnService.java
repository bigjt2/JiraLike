package com.jiralike.service;

import com.jiralike.dto.BoardColumnCreateDto;
import com.jiralike.dto.BoardColumnDto;
import com.jiralike.dto.TicketDto;
import com.jiralike.entity.BoardColumn;
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
public class BoardColumnService {

    private final BoardColumnRepository columnRepository;
    private final TicketRepository ticketRepository;
    private final ProjectService projectService;
    private final TicketService ticketService;

    @Transactional(readOnly = true)
    public List<BoardColumnDto> findByProject(Long projectId) {
        return columnRepository.findByProjectIdOrderByPositionAsc(projectId).stream()
                .map(this::toDtoWithTickets)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardColumnDto create(BoardColumnCreateDto dto) {
        int position = columnRepository.countByProjectId(dto.getProjectId());
        BoardColumn column = new BoardColumn();
        column.setName(dto.getName());
        column.setColor(dto.getColor());
        column.setPosition(position);
        column.setProject(projectService.getProjectOrThrow(dto.getProjectId()));
        return toDtoWithTickets(columnRepository.save(column));
    }

    @Transactional
    public BoardColumnDto update(Long id, BoardColumnCreateDto dto) {
        BoardColumn column = getColumnOrThrow(id);
        column.setName(dto.getName());
        if (dto.getColor() != null)
            column.setColor(dto.getColor());
        return toDtoWithTickets(columnRepository.save(column));
    }

    @Transactional
    public void delete(Long id) {
        getColumnOrThrow(id);
        columnRepository.deleteById(id);
    }

    private BoardColumn getColumnOrThrow(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found: " + id));
    }

    public BoardColumnDto toDtoWithTickets(BoardColumn column) {
        BoardColumnDto dto = projectService.toColumnDtoWithoutTickets(column);
        List<TicketDto> tickets = ticketRepository.findByColumnIdOrderByPositionAsc(column.getId()).stream()
                .map(ticketService::toDto)
                .collect(Collectors.toList());
        dto.setTickets(tickets);
        return dto;
    }
}
