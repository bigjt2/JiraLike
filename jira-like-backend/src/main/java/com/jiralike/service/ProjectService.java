package com.jiralike.service;

import com.jiralike.dto.BoardColumnDto;
import com.jiralike.dto.ProjectCreateDto;
import com.jiralike.dto.ProjectDto;
import com.jiralike.entity.BoardColumn;
import com.jiralike.entity.Project;
import com.jiralike.repository.BoardColumnRepository;
import com.jiralike.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final BoardColumnRepository columnRepository;

    public List<ProjectDto> findAll() {
        return projectRepository.findAll().stream()
                .map(this::toDtoWithoutTickets)
                .collect(Collectors.toList());
    }

    public ProjectDto findById(Long id) {
        Project project = getProjectOrThrow(id);
        return toDtoWithColumns(project);
    }

    public ProjectDto findByKey(String key) {
        Project project = projectRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + key));
        return toDtoWithColumns(project);
    }

    @Transactional
    public ProjectDto create(ProjectCreateDto dto) {
        if (projectRepository.existsByKey(dto.getKey())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project key already exists");
        }
        Project project = new Project();
        project.setName(dto.getName());
        project.setKey(dto.getKey().toUpperCase());
        project.setDescription(dto.getDescription());
        project = projectRepository.save(project);

        // Create default columns
        createDefaultColumns(project);

        return toDtoWithColumns(projectRepository.findById(project.getId()).orElseThrow());
    }

    @Transactional
    public ProjectDto update(Long id, ProjectCreateDto dto) {
        Project project = getProjectOrThrow(id);
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        return toDtoWithColumns(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        getProjectOrThrow(id);
        projectRepository.deleteById(id);
    }

    private void createDefaultColumns(Project project) {
        String[][] defaults = {
                {"To Do", "#6B7280"},
                {"In Progress", "#3B82F6"},
                {"In Review", "#F59E0B"},
                {"Done", "#10B981"}
        };
        for (int i = 0; i < defaults.length; i++) {
            BoardColumn col = new BoardColumn();
            col.setName(defaults[i][0]);
            col.setColor(defaults[i][1]);
            col.setPosition(i);
            col.setProject(project);
            columnRepository.save(col);
        }
    }

    public Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found: " + id));
    }

    private ProjectDto toDtoWithoutTickets(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setKey(project.getKey());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());
        return dto;
    }

    private ProjectDto toDtoWithColumns(Project project) {
        ProjectDto dto = toDtoWithoutTickets(project);
        List<BoardColumn> cols = columnRepository.findByProjectIdOrderByPositionAsc(project.getId());
        dto.setColumns(cols.stream().map(this::toColumnDtoWithoutTickets).collect(Collectors.toList()));
        return dto;
    }

    public BoardColumnDto toColumnDtoWithoutTickets(BoardColumn col) {
        BoardColumnDto dto = new BoardColumnDto();
        dto.setId(col.getId());
        dto.setName(col.getName());
        dto.setPosition(col.getPosition());
        dto.setColor(col.getColor());
        dto.setProjectId(col.getProject().getId());
        return dto;
    }
}
