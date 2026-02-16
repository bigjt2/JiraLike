package com.jiralike.repository;

import com.jiralike.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByKey(String key);
    boolean existsByKey(String key);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.columns c ORDER BY p.createdAt DESC")
    List<Project> findAllWithColumns();
}
