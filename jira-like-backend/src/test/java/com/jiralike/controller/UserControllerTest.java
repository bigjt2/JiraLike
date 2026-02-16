package com.jiralike.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralike.dto.UserCreateDto;
import com.jiralike.dto.UserDto;
import com.jiralike.service.UserService;
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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto buildUserDto(Long id, String username) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUsername(username);
        dto.setEmail(username + "@example.com");
        dto.setDisplayName("Display " + username);
        dto.setCreatedAt(Instant.now());
        return dto;
    }

    private UserCreateDto buildCreateDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("jdoe");
        dto.setEmail("jdoe@example.com");
        dto.setDisplayName("John Doe");
        return dto;
    }

    @Test
    void getAll_returnsListOfUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(buildUserDto(1L, "jdoe")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jdoe"));
    }

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returnsUser() throws Exception {
        when(userService.findById(1L)).thenReturn(buildUserDto(1L, "jdoe"));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jdoe"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(userService.create(any(UserCreateDto.class))).thenReturn(buildUserDto(2L, "jdoe"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jdoe"));
    }

    @Test
    void create_blankUsername_returns400() throws Exception {
        UserCreateDto dto = buildCreateDto();
        dto.setUsername("");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        UserCreateDto dto = buildCreateDto();
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicateUsername_returns409() throws Exception {
        when(userService.create(any(UserCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isConflict());
    }

    @Test
    void update_validRequest_returnsUpdatedUser() throws Exception {
        when(userService.update(eq(1L), any(UserCreateDto.class))).thenReturn(buildUserDto(1L, "jdoe"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(userService.update(eq(99L), any(UserCreateDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: 99"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: 99"))
                .when(userService).delete(99L);

        mockMvc.perform(delete("/api/users/99"))
                .andExpect(status().isNotFound());
    }
}
