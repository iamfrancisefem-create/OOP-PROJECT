package com.pms.repository;

import com.pms.entity.Notification;
import com.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Notification} entities.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndSeenFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    long countByUserAndSeenFalse(User user);

    Page<Notification> findByUser(User user, Pageable pageable);

    java.util.List<Notification> findByUserAndSeen(User user, boolean seen);

    /** Mark all unseen notifications for a user as seen in a single UPDATE. */
    @Modifying
    @Query("UPDATE Notification n SET n.seen = true WHERE n.user = :user AND n.seen = false")
    int markAllAsRead(@Param("user") User user);
}
