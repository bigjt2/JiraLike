package com.jiralike.service;

import com.jiralike.dto.UserCreateDto;
import com.jiralike.dto.UserDto;
import com.jiralike.entity.AppUser;
import com.jiralike.repository.AppUserRepository;
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
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private AppUser user;

    @BeforeEach
    void setUp() {
        user = new AppUser();
        user.setId(1L);
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.com");
        user.setDisplayName("John Doe");
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setCreatedAt(Instant.now());
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("jdoe");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("jdoe@example.com");
    }

    @Test
    void findById_missingId_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void create_newUser_savesAndReturnsDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setDisplayName("New User");
        dto.setAvatarUrl("https://example.com/new.png");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        UserDto result = userService.create(dto);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    void create_duplicateUsername_throwsConflict() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("jdoe");
        dto.setEmail("other@example.com");
        dto.setDisplayName("Dup");

        when(userRepository.existsByUsername("jdoe")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_duplicateEmail_throwsConflict() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("other");
        dto.setEmail("jdoe@example.com");
        dto.setDisplayName("Dup");

        when(userRepository.existsByUsername("other")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_existingUser_updatesFields() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("jdoe");
        dto.setEmail("jdoe@example.com");
        dto.setDisplayName("John Updated");
        dto.setAvatarUrl("https://example.com/new.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(AppUser.class))).thenReturn(user);

        UserDto result = userService.update(1L, dto);

        verify(userRepository).save(user);
        assertThat(result).isNotNull();
    }

    @Test
    void update_missingUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UserCreateDto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_existingUser_deletesById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_missingUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void getUserOrThrow_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AppUser result = userService.getUserOrThrow(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUserOrThrow_missing_throwsNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserOrThrow(5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void toDto_nullUser_returnsNull() {
        assertThat(userService.toDto(null)).isNull();
    }

    @Test
    void toDto_validUser_mapsAllFields() {
        UserDto dto = userService.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("jdoe");
        assertThat(dto.getEmail()).isEqualTo("jdoe@example.com");
        assertThat(dto.getDisplayName()).isEqualTo("John Doe");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(dto.getCreatedAt()).isNotNull();
    }
}
