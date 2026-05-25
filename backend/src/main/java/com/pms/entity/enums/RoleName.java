package com.pms.entity.enums;

/**
 * Enumeration of application-level user roles.
 *
 * <p>These are stored in the {@code roles} table and associated with users
 * through the {@code user_roles} join table. Spring Security prefixes
 * them with {@code ROLE_} when building granted authorities.</p>
 */
public enum RoleName {

    /** Full system access — user management, configuration, everything. */
    ADMIN,

    /** Can create/manage projects, assign teams, oversee milestones. */
    PROJECT_MANAGER,

    /** Leads a team — can assign tasks and manage team members. */
    TEAM_LEADER,

    /** Standard team participant — can view/update assigned tasks. */
    TEAM_MEMBER,

    /** Stakeholder role — can view dashboards and reports. */
    PRODUCT_OWNER
}
