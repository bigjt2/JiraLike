package com.jiralike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralike.dto.BoardColumnCreateDto;
import com.jiralike.dto.BoardColumnDto;
import com.jiralike.service.BoardColumnService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardColumnController.class)
class BoardColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardColumnService columnService;

    private BoardColumnDto buildColumnDto(Long id, String name) {
        BoardColumnDto dto = new BoardColumnDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPosition(0);
        dto.setColor("#6B7280");
        dto.setProjectId(1L);
        dto.setTickets(List.of());
        return dto;
    }

    private BoardColumnCreateDto buildCreateDto() {
        BoardColumnCreateDto dto = new BoardColumnCreateDto();
        dto.setName("To Do");
        dto.setColor("#6B7280");
        dto.setProjectId(1L);
        return dto;
    }

    @Test
    void getByProject_returnsColumnList() throws Exception {
        when(columnService.findByProject(1L)).thenReturn(List.of(buildColumnDto(10L, "To Do")));

        mockMvc.perform(get("/api/projects/1/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("To Do"));
    }

    @Test
    void getByProject_empty_returnsEmptyList() throws Exception {
        when(columnService.findByProject(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/projects/1/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(columnService.create(any(BoardColumnCreateDto.class))).thenReturn(buildColumnDto(10L, "To Do"));

        mockMvc.perform(post("/api/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("To Do"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        BoardColumnCreateDto dto = buildCreateDto();
        dto.setName("");

        mockMvc.perform(post("/api/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(columnService, never()).create(any());
    }

    @Test
    void create_nullProjectId_returns400() throws Exception {
        BoardColumnCreateDto dto = buildCreateDto();
        dto.setProjectId(null);

        mockMvc.perform(post("/api/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_projectNotFound_returns404() throws Exception {
        when(columnService.create(any(BoardColumnCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: 99"));

        mockMvc.perform(post("/api/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_validRequest_returnsUpdated() throws Exception {
        when(columnService.update(eq(10L), any(BoardColumnCreateDto.class))).thenReturn(buildColumnDto(10L, "Updated"));

        mockMvc.perform(put("/api/columns/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(columnService.update(eq(99L), any(BoardColumnCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found: 99"));

        mockMvc.perform(put("/api/columns/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        doNothing().when(columnService).delete(10L);

        mockMvc.perform(delete("/api/columns/10"))
                .andExpect(status().isNoContent());

        verify(columnService).delete(10L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found: 99"))
                .when(columnService).delete(99L);

        mockMvc.perform(delete("/api/columns/99"))
                .andExpect(status().isNotFound());
    }
}
