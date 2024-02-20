package nl.tudelft.healthblocks.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.tudelft.healthblocks.security.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * A class that is used to store the user data.
 * Username is used to log in or to register a user (researcher).
 * UserId is used internally to identify the user (i.e. also in the UserManagement service);
 *  it is also used as "Subject" in the JWT token used for the authentication.
 */
@Entity(name = "user-data")
@Getter @NoArgsConstructor
public class UserData implements UserDetails {

    @Getter
    @Id
    @Column(name = "username", length = 20, unique = true, nullable = false)
    private String username;

    @Getter
    @Column(name = "userId", unique = true, nullable = false, updatable = false)
    private long userId;

    @Setter
    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private UserRole role;

    @Setter
    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Setter
    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Setter
    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;

    @Setter
    @Column(name = "enabled")
    private boolean enabled;


    /**
     * Creates a UserData object with the specified values.
     *
     * @param username  the username of the user (a unique identifier used to log in)
     * @param userId    the ID of the user (a unique identifier used internally to identify the user)
     * @param password  the password of the user (will be hashed)
     * @param role      the role of the user ("admin" or "researcher")
     */
    public UserData(String username, long userId, String password, UserRole role) {
        this.username = username;
        this.userId = userId;
        this.password = password;
        this.role = role;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
    }

    /**
     * Gets the authorities granted to the user.
     *
     * @return the authorities granted to the user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(this.role.name()));
    }

    /**
     * Checks whether another user is equal to this user.
     *
     * @param other another user to compare to
     * @return true if another user is equal to this user, i.e. if their usernames are equal, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UserData otherUserData)) return false;
        return Objects.equals(this.username, otherUserData.username);
    }

    /**
     * Gets the hash code of the given UserData object.
     *
     * @return the hash code of the given UserData object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.username);
    }
}
