package com.pms.repository;

import com.pms.entity.Comment;
import com.pms.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Comment} entities.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByTask(Task task, Pageable pageable);

    long countByTask(Task task);
}
