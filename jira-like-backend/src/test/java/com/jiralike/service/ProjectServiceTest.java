package com.jiralike.service;

import com.jiralike.dto.BoardColumnDto;
import com.jiralike.dto.ProjectCreateDto;
import com.jiralike.dto.ProjectDto;
import com.jiralike.entity.BoardColumn;
import com.jiralike.entity.Project;
import com.jiralike.repository.BoardColumnRepository;
import com.jiralike.repository.ProjectRepository;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BoardColumnRepository columnRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setKey("TEST");
        project.setDescription("A test project");
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
    }

    @Test
    void findAll_returnsAllProjects() {
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<ProjectDto> result = projectService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey()).isEqualTo("TEST");
    }

    @Test
    void findAll_returnsEmpty() {
        when(projectRepository.findAll()).thenReturn(List.of());

        assertThat(projectService.findAll()).isEmpty();
    }

    @Test
    void findById_found_returnsDtoWithColumns() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of());

        ProjectDto result = projectService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getColumns()).isEmpty();
    }

    @Test
    void findById_notFound_throwsNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findByKey_found_returnsDtoWithColumns() {
        when(projectRepository.findByKey("TEST")).thenReturn(Optional.of(project));
        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of());

        ProjectDto result = projectService.findByKey("TEST");

        assertThat(result.getKey()).isEqualTo("TEST");
    }

    @Test
    void findByKey_notFound_throwsNotFound() {
        when(projectRepository.findByKey("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findByKey("MISSING"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void create_newProject_savesWithDefaultColumns() {
        ProjectCreateDto dto = new ProjectCreateDto();
        dto.setName("My Project");
        dto.setKey("MP");
        dto.setDescription("desc");

        when(projectRepository.existsByKey("MP")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(projectRepository.findById(2L)).thenReturn(Optional.of(project));
        when(columnRepository.findByProjectIdOrderByPositionAsc(any())).thenReturn(List.of());

        ProjectDto result = projectService.create(dto);

        assertThat(result).isNotNull();
        // 4 default columns should be saved
        verify(columnRepository, times(4)).save(any(BoardColumn.class));
    }

    @Test
    void create_duplicateKey_throwsConflict() {
        ProjectCreateDto dto = new ProjectCreateDto();
        dto.setKey("TEST");
        dto.setName("Another");

        when(projectRepository.existsByKey("TEST")).thenReturn(true);

        assertThatThrownBy(() -> projectService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void update_existingProject_updatesNameAndDescription() {
        ProjectCreateDto dto = new ProjectCreateDto();
        dto.setName("Updated Name");
        dto.setDescription("Updated desc");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);
        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of());

        ProjectDto result = projectService.update(1L, dto);

        assertThat(result).isNotNull();
        verify(projectRepository).save(project);
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.update(99L, new ProjectCreateDto()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_existingProject_deletesById() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void getProjectOrThrow_found_returnsProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getProjectOrThrow(1L);

        assertThat(result).isEqualTo(project);
    }

    @Test
    void getProjectOrThrow_missing_throwsNotFound() {
        when(projectRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectOrThrow(5L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void toColumnDtoWithoutTickets_mapsAllFields() {
        BoardColumn col = new BoardColumn();
        col.setId(10L);
        col.setName("To Do");
        col.setPosition(0);
        col.setColor("#6B7280");
        col.setProject(project);

        BoardColumnDto dto = projectService.toColumnDtoWithoutTickets(col);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("To Do");
        assertThat(dto.getPosition()).isEqualTo(0);
        assertThat(dto.getColor()).isEqualTo("#6B7280");
        assertThat(dto.getProjectId()).isEqualTo(1L);
    }

    @Test
    void findById_withColumns_returnsColumnsInDto() {
        BoardColumn col = new BoardColumn();
        col.setId(10L);
        col.setName("To Do");
        col.setPosition(0);
        col.setColor("#6B7280");
        col.setProject(project);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(columnRepository.findByProjectIdOrderByPositionAsc(1L)).thenReturn(List.of(col));

        ProjectDto result = projectService.findById(1L);

        assertThat(result.getColumns()).hasSize(1);
        assertThat(result.getColumns().get(0).getName()).isEqualTo("To Do");
    }
}
