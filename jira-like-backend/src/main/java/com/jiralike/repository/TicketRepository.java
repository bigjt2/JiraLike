package com.jiralike.repository;

import com.jiralike.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByProjectIdOrderByPositionAsc(Long projectId);
    List<Ticket> findByColumnIdOrderByPositionAsc(Long columnId);

    @Query("SELECT t FROM Ticket t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.reporter WHERE t.project.id = :projectId ORDER BY t.column.position ASC, t.position ASC")
    List<Ticket> findByProjectIdWithUsers(@Param("projectId") Long projectId);

    int countByColumnId(Long columnId);

    @Query("SELECT MAX(t.position) FROM Ticket t WHERE t.column.id = :columnId")
    Integer findMaxPositionInColumn(@Param("columnId") Long columnId);
}
