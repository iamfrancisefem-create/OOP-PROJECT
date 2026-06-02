package com.pms.entity;

import com.pms.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_invitations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformInvitation extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleName role;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    private User invitedBy;

    @Builder.Default
    @Column(nullable = false)
    private Boolean accepted = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
