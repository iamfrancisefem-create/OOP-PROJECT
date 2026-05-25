package com.pms.entity;

import com.pms.entity.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core user entity — also implements {@link UserDetails} so that
 * Spring Security can authenticate and authorize directly from the DB.
 *
 * <p>Security-related flags ({@code enabled}, {@code accountLocked},
 * {@code failedLoginAttempts}) support account lockout after repeated
 * failed logins and email-verification workflows.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image")
    private String profileImage;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = false;

    @Builder.Default
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    // ------------------------------------------------------------------ //
    // Relationships                                                      //
    // ------------------------------------------------------------------ //

    /** Roles are eagerly fetched because they are needed on every request. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // ------------------------------------------------------------------ //
    // UserDetails implementation                                         //
    // ------------------------------------------------------------------ //

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
    }

    /** Spring Security uses email as the username. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
