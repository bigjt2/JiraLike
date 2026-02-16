package com.jiralike.service;

import com.jiralike.dto.BoardColumnCreateDto;
import com.jiralike.dto.BoardColumnDto;
import com.jiralike.dto.TicketDto;
import com.jiralike.entity.BoardColumn;
import com.jiralike.entity.Project;
import com.jiralike.entity.Ticket;
import com.jiralike.repository.BoardColumnRepository;
import com.jiralike.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardColumnServiceTest {

    @Mock
    private BoardColumnRepository columnRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private BoardColumnService boardColumnService;

    private Project project;
    private BoardColumn column;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setKey("TEST");

        column = new BoardColumn();
        column.setId(10L);
        column.setName("To Do");
        column.setPosition(0);
        column.setColor("#6B7280");
        column.setProject(project);
    }

    @Test
    void findByProject_returnsMappedColumnsWithTickets() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("T1");
        ticket.setProject(project);
        ticket.setColumn(column);

        BoardColumnDto colDto = new BoardColumnDto();
        colDto.setId(10L);
        colDto.setName("To Do");
        colDto.setProjectId(1L);

        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of(column));
        when(ticketRepository.findByColumnIdOrderByPositionAsc(10L)).thenReturn(List.of(ticket));
        when(projectService.toColumnDtoWithoutTickets(column)).thenReturn(colDto);
        when(ticketService.toDto(ticket)).thenReturn(new TicketDto());

        List<BoardColumnDto> result = boardColumnService.findByProject(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTickets()).hasSize(1);
    }

    @Test
    void findByProject_empty_returnsEmpty() {
        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of());

        assertThat(boardColumnService.findByProject(1L)).isEmpty();
    }

    @Test
    void create_setsPositionAndSaves() {
        BoardColumnCreateDto dto = new BoardColumnCreateDto();
        dto.setName("New Column");
        dto.setColor("#FF0000");
        dto.setProjectId(1L);

        BoardColumnDto colDto = new BoardColumnDto();
        colDto.setId(20L);
        colDto.setName("New Column");
        colDto.setProjectId(1L);

        when(columnRepository.countByProjectId(1L)).thenReturn(3);
        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(columnRepository.save(any(BoardColumn.class))).thenAnswer(inv -> {
            BoardColumn c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });
        when(ticketRepository.findByColumnIdOrderByPositionAsc(20L)).thenReturn(List.of());
        when(projectService.toColumnDtoWithoutTickets(any(BoardColumn.class))).thenReturn(colDto);

        BoardColumnDto result = boardColumnService.create(dto);

        assertThat(result).isNotNull();
        verify(columnRepository).save(argThat(c -> c.getPosition() == 3));
    }

    @Test
    void update_withColor_updatesNameAndColor() {
        BoardColumnCreateDto dto = new BoardColumnCreateDto();
        dto.setName("Updated");
        dto.setColor("#123456");
        dto.setProjectId(1L);

        BoardColumnDto colDto = new BoardColumnDto();
        colDto.setId(10L);
        colDto.setName("Updated");
        colDto.setProjectId(1L);

        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(columnRepository.save(column)).thenReturn(column);
        when(ticketRepository.findByColumnIdOrderByPositionAsc(10L)).thenReturn(List.of());
        when(projectService.toColumnDtoWithoutTickets(column)).thenReturn(colDto);

        BoardColumnDto result = boardColumnService.update(10L, dto);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(column.getColor()).isEqualTo("#123456");
    }

    @Test
    void update_withNullColor_doesNotChangeColor() {
        BoardColumnCreateDto dto = new BoardColumnCreateDto();
        dto.setName("Updated");
        dto.setColor(null);
        dto.setProjectId(1L);

        BoardColumnDto colDto = new BoardColumnDto();
        colDto.setId(10L);
        colDto.setName("Updated");

        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(columnRepository.save(column)).thenReturn(column);
        when(ticketRepository.findByColumnIdOrderByPositionAsc(10L)).thenReturn(List.of());
        when(projectService.toColumnDtoWithoutTickets(column)).thenReturn(colDto);

        boardColumnService.update(10L, dto);

        assertThat(column.getColor()).isEqualTo("#6B7280"); // unchanged
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardColumnService.update(99L, new BoardColumnCreateDto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_existing_deletesById() {
        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));

        boardColumnService.delete(10L);

        verify(columnRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardColumnService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(columnRepository, never()).deleteById(any());
    }

    @Test
    void toDtoWithTickets_returnsColumnWithEmptyTickets() {
        BoardColumnDto colDto = new BoardColumnDto();
        colDto.setId(10L);
        colDto.setName("To Do");
        colDto.setProjectId(1L);

        when(projectService.toColumnDtoWithoutTickets(column)).thenReturn(colDto);
        when(ticketRepository.findByColumnIdOrderByPositionAsc(10L)).thenReturn(List.of());

        BoardColumnDto result = boardColumnService.toDtoWithTickets(column);

        assertThat(result.getTickets()).isEmpty();
    }
}
