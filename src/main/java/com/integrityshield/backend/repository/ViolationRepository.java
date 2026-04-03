package com.integrityshield.backend.repository;

import com.integrityshield.backend.entity.Violation;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ViolationRepository
        extends JpaRepository<Violation, Long> {

    long countBySessionIdAndStudentRoll(
            Long sessionId,
            String studentRoll
    );

    List<Violation> findBySessionIdAndLevel(
            Long sessionId,
            String level
    );

    List<Violation> findBySessionId(
            Long sessionId
    );

    @Query("""
    	    SELECT v.studentRoll, COUNT(v)
    	    FROM Violation v
    	    WHERE v.sessionId = :sessionId
    	    AND v.level = 'ESCALATED'
    	    GROUP BY v.studentRoll
    	""")
    	List<Object[]> findSessionReport(@Param("sessionId") Long sessionId);

    @Modifying
    @Transactional
    void deleteByTimestampBefore(LocalDateTime time);
}