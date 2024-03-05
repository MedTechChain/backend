package nl.tudelft.medtechchain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;

public class UserDataTest {

    private UserData user;

    @BeforeEach
    public void setup() {
        user = new UserData("jdoe", "plaintextfornow", "john.doe@tudelft.nl",
                "John", "Doe", "Delft University of Technology", UserRole.RESEARCHER);
    }

    @Test
    public void getUsername() {
        Assertions.assertThat(user.getUsername()).isEqualTo("jdoe");
    }

    @Test
    public void getPassword() {
        Assertions.assertThat(user.getPassword()).isEqualTo("plaintextfornow");
    }

    @Test
    public void getEmail() {
        Assertions.assertThat(user.getEmail()).isEqualTo("john.doe@tudelft.nl");
    }

    @Test
    public void getFirstName() {
        Assertions.assertThat(user.getFirstName()).isEqualTo("John");
    }

    @Test
    public void getLastName() {
        Assertions.assertThat(user.getLastName()).isEqualTo("Doe");
    }

    @Test
    public void getAffiliation() {
        Assertions.assertThat(user.getAffiliation()).isEqualTo("Delft University of Technology");
    }

    @Test
    public void getRole() {
        Assertions.assertThat(user.getRole()).isEqualTo(UserRole.RESEARCHER);
    }

    @Test
    public void isAccountNonExpired() {
        Assertions.assertThat(user.isAccountNonExpired()).isTrue();
    }

    @Test
    public void isAccountNonLocked() {
        Assertions.assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    public void isCredentialsNonExpired() {
        Assertions.assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    public void isEnabled() {
        Assertions.assertThat(user.isEnabled()).isTrue();
    }

    @Test
    public void setPassword() {
        user.setPassword("thisisalsonotencrypted");
        Assertions.assertThat(user.getPassword()).isEqualTo("thisisalsonotencrypted");
    }

    @Test
    public void setFirstName() {
        user.setFirstName("Johnny");
        Assertions.assertThat(user.getFirstName()).isEqualTo("Johnny");
    }

    @Test
    public void setLastName() {
        user.setLastName("Roe");
        Assertions.assertThat(user.getLastName()).isEqualTo("Roe");
    }

    @Test
    public void setAffiliation() {
        user.setAffiliation("Eidgenössische Technische Hochschule Zürich");
        Assertions.assertThat(user.getAffiliation())
                .isEqualTo("Eidgenössische Technische Hochschule Zürich");
    }

    @Test
    public void setAccountNonExpired() {
        user.setAccountNonExpired(false);
        Assertions.assertThat(user.isAccountNonExpired()).isFalse();
    }

    @Test
    public void setAccountNonLocked() {
        user.setAccountNonLocked(false);
        Assertions.assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    public void setCredentialsNonExpired() {
        user.setCredentialsNonExpired(false);
        Assertions.assertThat(user.isCredentialsNonExpired()).isFalse();
    }

    @Test
    public void setEnabled() {
        user.setEnabled(false);
        Assertions.assertThat(user.isEnabled()).isFalse();
    }

    @Test
    public void getAuthorities() {
        Collection<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority(UserRole.RESEARCHER.name())
        );
        Assertions.assertThat(user.getAuthorities()).isEqualTo(authorities);
    }

    @Test
    void testEqualsSameObject() {
        Assertions.assertThat(user).isEqualTo(user);
    }

    @Test
    void testEqualsOtherObject() {
        Assertions.assertThat(user).isNotEqualTo("user");
    }

    // Other tests for equals and hashCode can be found in AuthenticationServiceTest
    //   (due to the fact that a repository is needed to generate userID, which should be tested in
    //   an integration test and not in a unit test).
}
