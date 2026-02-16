package com.jiralike.service;

import com.jiralike.dto.CommentCreateDto;
import com.jiralike.dto.CommentDto;
import com.jiralike.dto.UserDto;
import com.jiralike.entity.AppUser;
import com.jiralike.entity.BoardColumn;
import com.jiralike.entity.Comment;
import com.jiralike.entity.Project;
import com.jiralike.entity.Ticket;
import com.jiralike.repository.CommentRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    private Ticket ticket;
    private AppUser author;
    private Comment comment;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setId(1L);
        project.setKey("TEST");

        BoardColumn column = new BoardColumn();
        column.setId(10L);
        column.setProject(project);

        ticket = new Ticket();
        ticket.setId(100L);
        ticket.setTitle("Some ticket");
        ticket.setProject(project);
        ticket.setColumn(column);

        author = new AppUser();
        author.setId(5L);
        author.setUsername("jdoe");
        author.setEmail("jdoe@example.com");
        author.setDisplayName("John Doe");

        comment = new Comment();
        comment.setId(200L);
        comment.setContent("Great work!");
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
    }

    @Test
    void findByTicket_returnsMappedComments() {
        UserDto authorDto = new UserDto();
        authorDto.setId(5L);
        authorDto.setUsername("jdoe");

        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(comment));
        when(userService.toDto(author)).thenReturn(authorDto);

        List<CommentDto> result = commentService.findByTicket(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("Great work!");
    }

    @Test
    void findByTicket_empty_returnsEmpty() {
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        assertThat(commentService.findByTicket(100L)).isEmpty();
    }

    @Test
    void create_validRequest_savesAndReturnsDto() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("New comment");
        dto.setAuthorId(5L);

        UserDto authorDto = new UserDto();
        authorDto.setId(5L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userService.getUserOrThrow(5L)).thenReturn(author);
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(201L);
            return c;
        });
        when(userService.toDto(author)).thenReturn(authorDto);

        CommentDto result = commentService.create(100L, dto);

        assertThat(result.getContent()).isEqualTo("New comment");
        assertThat(result.getTicketId()).isEqualTo(100L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_ticketNotFound_throwsNotFound() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("text");
        dto.setAuthorId(5L);

        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(99L, dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_existingComment_updatesContent() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("Updated content");
        dto.setAuthorId(5L);

        UserDto authorDto = new UserDto();
        authorDto.setId(5L);

        when(commentRepository.findById(200L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(userService.toDto(author)).thenReturn(authorDto);

        CommentDto result = commentService.update(200L, dto);

        assertThat(result.getContent()).isEqualTo("Updated content");
        verify(commentRepository).save(comment);
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(99L, new CommentCreateDto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_existingComment_deletesById() {
        when(commentRepository.findById(200L)).thenReturn(Optional.of(comment));

        commentService.delete(200L);

        verify(commentRepository).deleteById(200L);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void toDto_mapsAllFields() {
        UserDto authorDto = new UserDto();
        authorDto.setId(5L);
        authorDto.setUsername("jdoe");

        when(userService.toDto(author)).thenReturn(authorDto);

        CommentDto dto = commentService.toDto(comment);

        assertThat(dto.getId()).isEqualTo(200L);
        assertThat(dto.getContent()).isEqualTo("Great work!");
        assertThat(dto.getTicketId()).isEqualTo(100L);
        assertThat(dto.getAuthor().getUsername()).isEqualTo("jdoe");
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }
}
