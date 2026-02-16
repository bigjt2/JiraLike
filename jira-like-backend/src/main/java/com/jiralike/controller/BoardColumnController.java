package com.jiralike.controller;

import com.jiralike.dto.BoardColumnCreateDto;
import com.jiralike.dto.BoardColumnDto;
import com.jiralike.service.BoardColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService columnService;

    @GetMapping("/projects/{projectId}/columns")
    public List<BoardColumnDto> getByProject(@PathVariable Long projectId) {
        return columnService.findByProject(projectId);
    }

    @PostMapping("/columns")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardColumnDto create(@Valid @RequestBody BoardColumnCreateDto dto) {
        return columnService.create(dto);
    }

    @PutMapping("/columns/{id}")
    public BoardColumnDto update(@PathVariable Long id, @Valid @RequestBody BoardColumnCreateDto dto) {
        return columnService.update(id, dto);
    }

    @DeleteMapping("/columns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        columnService.delete(id);
    }
}
