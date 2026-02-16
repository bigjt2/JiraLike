package com.jiralike.service;

import com.jiralike.dto.TicketCreateDto;
import com.jiralike.dto.TicketDto;
import com.jiralike.dto.TicketMoveDto;
import com.jiralike.entity.*;
import com.jiralike.entity.Ticket.Priority;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TicketService ticketService;

    private Project project;
    private BoardColumn column;
    private Ticket ticket;
    private AppUser assignee;

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
        column.setProject(project);

        assignee = new AppUser();
        assignee.setId(5L);
        assignee.setUsername("jdoe");
        assignee.setEmail("jdoe@example.com");
        assignee.setDisplayName("John Doe");

        ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTitle("Fix bug");
        ticket.setDescription("A bug to fix");
        ticket.setPriority(Ticket.Priority.HIGH);
        ticket.setTicketType(Ticket.TicketType.BUG);
        ticket.setPosition(0);
        ticket.setProject(project);
        ticket.setColumn(column);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
    }

    @Test
    void findByProject_returnsMappedTickets() {
        when(ticketRepository.findByProjectIdWithUsers(1L)).thenReturn(List.of(ticket));

        List<TicketDto> result = ticketService.findByProject(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Fix bug");
    }

    @Test
    void findByProject_empty_returnsEmptyList() {
        when(ticketRepository.findByProjectIdWithUsers(1L)).thenReturn(List.of());

        assertThat(ticketService.findByProject(1L)).isEmpty();
    }

    @Test
    void findById_found_returnsDto() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        TicketDto result = ticketService.findById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findById_notFound_throwsNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void create_withAssigneeAndReporter_savesTicket() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("New ticket");
        dto.setProjectId(1L);
        dto.setColumnId(10L);
        dto.setAssigneeId(5L);
        dto.setReporterId(5L);
        dto.setPriority(Ticket.Priority.LOW);
        dto.setTicketType(Ticket.TicketType.STORY);
        dto.setStoryPoints(3);
        dto.setDueDate(LocalDate.now().plusDays(7));

        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(ticketRepository.findMaxPositionInColumn(10L)).thenReturn(2);
        when(userService.getUserOrThrow(5L)).thenReturn(assignee);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(101L);
            return t;
        });

        TicketDto result = ticketService.create(dto);

        assertThat(result.getTitle()).isEqualTo("New ticket");
        assertThat(result.getPosition()).isEqualTo(3);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void create_withNullMaxPosition_setsPositionToZero() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("First ticket");
        dto.setProjectId(1L);
        dto.setColumnId(10L);

        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(ticketRepository.findMaxPositionInColumn(10L)).thenReturn(null);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(102L);
            return t;
        });

        TicketDto result = ticketService.create(dto);

        assertThat(result.getPosition()).isEqualTo(0);
    }

    @Test
    void create_withDefaultPriorityAndType_usesDefaults() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Default ticket");
        dto.setProjectId(1L);
        dto.setColumnId(10L);
        dto.setPriority(null);
        dto.setTicketType(null);

        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(ticketRepository.findMaxPositionInColumn(10L)).thenReturn(null);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDto result = ticketService.create(dto);

        assertThat(result.getPriority()).isEqualTo(Ticket.Priority.MEDIUM);
        assertThat(result.getTicketType()).isEqualTo(Ticket.TicketType.TASK);
    }

    @Test
    void create_columnNotFound_throwsNotFound() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("t");
        dto.setProjectId(1L);
        dto.setColumnId(99L);

        when(projectService.getProjectOrThrow(1L)).thenReturn(project);
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void update_changingColumn_updatesColumnAndFields() {
        BoardColumn newColumn = new BoardColumn();
        newColumn.setId(20L);
        newColumn.setName("In Progress");
        newColumn.setProject(project);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setPriority(Ticket.Priority.CRITICAL);
        dto.setTicketType(Ticket.TicketType.BUG);
        dto.setColumnId(20L);
        dto.setAssigneeId(5L);
        dto.setReporterId(5L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(columnRepository.findById(20L)).thenReturn(Optional.of(newColumn));
        when(userService.getUserOrThrow(5L)).thenReturn(assignee);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDto result = ticketService.update(100L, dto);

        assertThat(result).isNotNull();
        verify(columnRepository).findById(20L);
    }

    @Test
    void update_changingColumn_withNullValues() {
        BoardColumn newColumn = new BoardColumn();
        newColumn.setId(20L);
        newColumn.setName("In Progress");
        newColumn.setProject(project);

        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setColumnId(20L);
        dto.setAssigneeId(5L);
        dto.setReporterId(5L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(columnRepository.findById(20L)).thenReturn(Optional.of(newColumn));
        when(userService.getUserOrThrow(5L)).thenReturn(assignee);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketDto result = ticketService.update(100L, dto);

        assertThat(result).isNotNull();
        verify(columnRepository).findById(20L);
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertEquals(Ticket.TicketType.TASK, result.getTicketType());
    }

    @Test
    void update_nullAssignee_clearsAssignee() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Updated");
        dto.setColumnId(10L);
        dto.setAssigneeId(null);
        dto.setReporterId(null);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.update(100L, dto);

        verify(ticketRepository).save(argThat(t -> t.getAssignee() == null));
    }

    @Test
    void update_sameColumn_doesNotFetchNewColumn() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("Same col");
        dto.setColumnId(10L); // same as current column
        dto.setAssigneeId(null);
        dto.setReporterId(null);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.update(100L, dto);

        verify(columnRepository, never()).findById(any());
    }

    @Test
    void update_nullColumnId_doesNotChangeColumn() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("No col");
        dto.setColumnId(null);
        dto.setAssigneeId(null);
        dto.setReporterId(null);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.update(100L, dto);

        verify(columnRepository, never()).findById(any());
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.update(99L, new TicketCreateDto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void move_shiftsPositionsAndSaves() {
        Ticket other = new Ticket();
        other.setId(200L);
        other.setPosition(2);
        other.setProject(project);
        other.setColumn(column);

        TicketMoveDto dto = new TicketMoveDto();
        dto.setColumnId(10L);
        dto.setPosition(2);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(columnRepository.findById(10L)).thenReturn(Optional.of(column));
        when(ticketRepository.findByColumnIdOrderByPositionAsc(10L))
                .thenReturn(new ArrayList<>(List.of(ticket, other)));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketDto result = ticketService.move(100L, dto);

        assertThat(result).isNotNull();
        assertThat(other.getPosition()).isEqualTo(3);
    }

    @Test
    void move_columnNotFound_throwsNotFound() {
        TicketMoveDto dto = new TicketMoveDto();
        dto.setColumnId(99L);
        dto.setPosition(0);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.move(100L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_existing_deletesById() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        ticketService.delete(100L);

        verify(ticketRepository).deleteById(100L);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(ticketRepository, never()).deleteById(any());
    }

    @Test
    void toDto_mapsAllFields() {
        ticket.setAssignee(assignee);
        ticket.setReporter(assignee);

        when(userService.toDto(assignee)).thenCallRealMethod();

        TicketDto dto = ticketService.toDto(ticket);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Fix bug");
        assertThat(dto.getProjectId()).isEqualTo(1L);
        assertThat(dto.getProjectKey()).isEqualTo("TEST");
        assertThat(dto.getColumnId()).isEqualTo(10L);
        assertThat(dto.getColumnName()).isEqualTo("To Do");
    }

    @Test
    void toDto_nullAssignee_assigneeDtoIsNull() {
        ticket.setAssignee(null);
        when(userService.toDto(null)).thenReturn(null);

        TicketDto dto = ticketService.toDto(ticket);

        assertThat(dto.getAssignee()).isNull();
    }
}
