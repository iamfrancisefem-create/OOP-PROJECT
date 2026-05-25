package com.pms.entity.enums;

/**
 * Categories of in-app notifications.
 *
 * <p>Used to determine display formatting on the frontend
 * and to filter notification preferences.</p>
 */
public enum NotificationType {

    TASK_ASSIGNED,
    TASK_UPDATED,
    DEADLINE_APPROACHING,
    DEADLINE_OVERDUE,
    PROJECT_CREATED,
    PROJECT_UPDATED,
    MESSAGE_RECEIVED,
    TEAM_INVITATION,
    COMMENT_ADDED,
    SYSTEM
}
