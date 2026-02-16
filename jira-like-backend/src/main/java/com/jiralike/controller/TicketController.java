package com.jiralike.controller;

import com.jiralike.dto.*;
import com.jiralike.service.CommentService;
import com.jiralike.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService;

    @GetMapping("/projects/{projectId}/tickets")
    public List<TicketDto> getByProject(@PathVariable Long projectId) {
        return ticketService.findByProject(projectId);
    }

    @GetMapping("/tickets/{id}")
    public TicketDto getById(@PathVariable Long id) {
        return ticketService.findById(id);
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDto create(@Valid @RequestBody TicketCreateDto dto) {
        return ticketService.create(dto);
    }

    @PutMapping("/tickets/{id}")
    public TicketDto update(@PathVariable Long id, @Valid @RequestBody TicketCreateDto dto) {
        return ticketService.update(id, dto);
    }

    @PatchMapping("/tickets/{id}/move")
    public TicketDto move(@PathVariable Long id, @Valid @RequestBody TicketMoveDto dto) {
        return ticketService.move(id, dto);
    }

    @DeleteMapping("/tickets/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ticketService.delete(id);
    }

    // Comments nested under tickets
    @GetMapping("/tickets/{ticketId}/comments")
    public List<CommentDto> getComments(@PathVariable Long ticketId) {
        return commentService.findByTicket(ticketId);
    }

    @PostMapping("/tickets/{ticketId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long ticketId, @Valid @RequestBody CommentCreateDto dto) {
        return commentService.create(ticketId, dto);
    }

    @PutMapping("/comments/{id}")
    public CommentDto updateComment(@PathVariable Long id, @Valid @RequestBody CommentCreateDto dto) {
        return commentService.update(id, dto);
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long id) {
        commentService.delete(id);
    }
}
