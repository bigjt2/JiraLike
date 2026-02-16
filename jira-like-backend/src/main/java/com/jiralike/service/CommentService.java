package com.jiralike.service;

import com.jiralike.dto.CommentCreateDto;
import com.jiralike.dto.CommentDto;
import com.jiralike.entity.Comment;
import com.jiralike.entity.Ticket;
import com.jiralike.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;

    public List<CommentDto> findByTicket(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto create(Long ticketId, CommentCreateDto dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: " + ticketId));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setTicket(ticket);
        comment.setAuthor(userService.getUserOrThrow(dto.getAuthorId()));

        return toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto update(Long id, CommentCreateDto dto) {
        Comment comment = getCommentOrThrow(id);
        comment.setContent(dto.getContent());
        return toDto(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long id) {
        getCommentOrThrow(id);
        commentRepository.deleteById(id);
    }

    private Comment getCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found: " + id));
    }

    public CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setTicketId(comment.getTicket().getId());
        dto.setAuthor(userService.toDto(comment.getAuthor()));
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
