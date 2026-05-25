package com.pms.entity.enums;

/**
 * Lifecycle states for a {@link com.pms.entity.Project}.
 */
public enum ProjectStatus {

    /** Newly created, not yet started. */
    NEW,

    /** Work is actively in progress. */
    ACTIVE,

    /** Temporarily paused. */
    ON_HOLD,

    /** All deliverables finished. */
    COMPLETED,

    /** Project was abandoned or cancelled. */
    CANCELLED
}
