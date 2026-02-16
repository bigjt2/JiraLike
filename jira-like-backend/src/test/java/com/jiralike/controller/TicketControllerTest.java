package com.jiralike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralike.dto.*;
import com.jiralike.entity.Ticket;
import com.jiralike.service.CommentService;
import com.jiralike.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CommentService commentService;

    private TicketDto buildTicketDto(Long id) {
        TicketDto dto = new TicketDto();
        dto.setId(id);
        dto.setTitle("Ticket " + id);
        dto.setPriority(Ticket.Priority.MEDIUM);
        dto.setTicketType(Ticket.TicketType.TASK);
        dto.setProjectId(1L);
        dto.setProjectKey("TEST");
        dto.setColumnId(10L);
        dto.setColumnName("To Do");
        dto.setPosition(0);
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    private TicketCreateDto buildCreateDto() {
        TicketCreateDto dto = new TicketCreateDto();
        dto.setTitle("New Ticket");
        dto.setProjectId(1L);
        dto.setColumnId(10L);
        return dto;
    }

    private CommentDto buildCommentDto(Long id) {
        CommentDto dto = new CommentDto();
        dto.setId(id);
        dto.setContent("Comment " + id);
        dto.setTicketId(100L);
        dto.setCreatedAt(Instant.now());
        return dto;
    }

    @Test
    void getByProject_returnsTickets() throws Exception {
        when(ticketService.findByProject(1L)).thenReturn(List.of(buildTicketDto(1L)));

        mockMvc.perform(get("/api/projects/1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByProject_empty_returnsEmptyList() throws Exception {
        when(ticketService.findByProject(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/projects/1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returnsTicket() throws Exception {
        when(ticketService.findById(1L)).thenReturn(buildTicketDto(1L));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ticket 1"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(ticketService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: 99"));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(ticketService.create(any(TicketCreateDto.class))).thenReturn(buildTicketDto(2L));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void create_blankTitle_returns400() throws Exception {
        TicketCreateDto dto = buildCreateDto();
        dto.setTitle("");

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).create(any());
    }

    @Test
    void create_nullProjectId_returns400() throws Exception {
        TicketCreateDto dto = buildCreateDto();
        dto.setProjectId(null);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validRequest_returnsUpdated() throws Exception {
        when(ticketService.update(eq(1L), any(TicketCreateDto.class))).thenReturn(buildTicketDto(1L));

        mockMvc.perform(put("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(ticketService.update(eq(99L), any(TicketCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: 99"));

        mockMvc.perform(put("/api/tickets/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void move_validRequest_returnsMovedTicket() throws Exception {
        TicketMoveDto moveDto = new TicketMoveDto();
        moveDto.setColumnId(20L);
        moveDto.setPosition(1);

        when(ticketService.move(eq(1L), any(TicketMoveDto.class))).thenReturn(buildTicketDto(1L));

        mockMvc.perform(patch("/api/tickets/1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void move_nullColumnId_returns400() throws Exception {
        TicketMoveDto moveDto = new TicketMoveDto();
        moveDto.setColumnId(null);
        moveDto.setPosition(1);

        mockMvc.perform(patch("/api/tickets/1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        doNothing().when(ticketService).delete(1L);

        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isNoContent());

        verify(ticketService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: 99"))
                .when(ticketService).delete(99L);

        mockMvc.perform(delete("/api/tickets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_returnsComments() throws Exception {
        when(commentService.findByTicket(100L)).thenReturn(List.of(buildCommentDto(1L)));

        mockMvc.perform(get("/api/tickets/100/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Comment 1"));
    }

    @Test
    void addComment_validRequest_returns201() throws Exception {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("Great work!");
        dto.setAuthorId(5L);

        when(commentService.create(eq(100L), any(CommentCreateDto.class))).thenReturn(buildCommentDto(1L));

        mockMvc.perform(post("/api/tickets/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addComment_blankContent_returns400() throws Exception {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("");
        dto.setAuthorId(5L);

        mockMvc.perform(post("/api/tickets/100/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).create(any(), any());
    }

    @Test
    void updateComment_validRequest_returnsUpdated() throws Exception {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setContent("Updated");
        dto.setAuthorId(5L);

        when(commentService.update(eq(1L), any(CommentCreateDto.class))).thenReturn(buildCommentDto(1L));

        mockMvc.perform(put("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteComment_existing_returns204() throws Exception {
        doNothing().when(commentService).delete(1L);

        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService).delete(1L);
    }

    @Test
    void deleteComment_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: 99"))
                .when(commentService).delete(99L);

        mockMvc.perform(delete("/api/comments/99"))
                .andExpect(status().isNotFound());
    }
}
