package com.pms.entity;

import com.pms.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an application role (e.g. ADMIN, PROJECT_MANAGER).
 *
 * <p>Roles are stored as a separate table rather than a simple enum column
 * on the user, enabling a many-to-many relationship where a single user
 * can hold multiple roles simultaneously.</p>
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;
}
