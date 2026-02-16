package com.jiralike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralike.dto.ProjectCreateDto;
import com.jiralike.dto.ProjectDto;
import com.jiralike.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    private ProjectDto buildProjectDto(Long id, String key) {
        ProjectDto dto = new ProjectDto();
        dto.setId(id);
        dto.setName("Project " + key);
        dto.setKey(key);
        dto.setDescription("desc");
        dto.setCreatedAt(Instant.now());
        dto.setUpdatedAt(Instant.now());
        return dto;
    }

    private ProjectCreateDto buildCreateDto() {
        ProjectCreateDto dto = new ProjectCreateDto();
        dto.setName("My Project");
        dto.setKey("MP");
        dto.setDescription("A project");
        return dto;
    }

    @Test
    void getAll_returnsProjectList() throws Exception {
        when(projectService.findAll()).thenReturn(List.of(buildProjectDto(1L, "TEST")));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("TEST"));
    }

    @Test
    void getAll_returnsEmpty() throws Exception {
        when(projectService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returnsProject() throws Exception {
        when(projectService.findById(1L)).thenReturn(buildProjectDto(1L, "TEST"));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.key").value("TEST"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(projectService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: 99"));

        mockMvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByKey_found_returnsProject() throws Exception {
        when(projectService.findByKey("TEST")).thenReturn(buildProjectDto(1L, "TEST"));

        mockMvc.perform(get("/api/projects/key/TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("TEST"));
    }

    @Test
    void getByKey_notFound_returns404() throws Exception {
        when(projectService.findByKey("MISSING"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: MISSING"));

        mockMvc.perform(get("/api/projects/key/MISSING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(projectService.create(any(ProjectCreateDto.class))).thenReturn(buildProjectDto(2L, "MP"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("MP"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        ProjectCreateDto dto = buildCreateDto();
        dto.setName("");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).create(any());
    }

    @Test
    void create_duplicateKey_returns409() throws Exception {
        when(projectService.create(any(ProjectCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Project key already exists"));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isConflict());
    }

    @Test
    void update_validRequest_returnsUpdated() throws Exception {
        when(projectService.update(eq(1L), any(ProjectCreateDto.class))).thenReturn(buildProjectDto(1L, "TEST"));

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(projectService.update(eq(99L), any(ProjectCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: 99"));

        mockMvc.perform(put("/api/projects/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        doNothing().when(projectService).delete(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: 99"))
                .when(projectService).delete(99L);

        mockMvc.perform(delete("/api/projects/99"))
                .andExpect(status().isNotFound());
    }
}
