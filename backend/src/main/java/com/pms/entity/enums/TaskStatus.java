package com.pms.entity.enums;

/**
 * Workflow states for a {@link com.pms.entity.Task}.
 *
 * <p>Tasks progress through these states linearly:
 * {@code TODO → IN_PROGRESS → TESTING → DONE}.</p>
 */
public enum TaskStatus {

    TODO,
    IN_PROGRESS,
    TESTING,
    DONE
}
