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
    public void testGetUsername() {
        Assertions.assertThat(user.getUsername()).isEqualTo("jdoe");
    }

    @Test
    public void testGetPassword() {
        Assertions.assertThat(user.getPassword()).isEqualTo("plaintextfornow");
    }

    @Test
    public void testGetEmail() {
        Assertions.assertThat(user.getEmail()).isEqualTo("john.doe@tudelft.nl");
    }

    @Test
    public void testGetFirstName() {
        Assertions.assertThat(user.getFirstName()).isEqualTo("John");
    }

    @Test
    public void testGetLastName() {
        Assertions.assertThat(user.getLastName()).isEqualTo("Doe");
    }

    @Test
    public void testGetAffiliation() {
        Assertions.assertThat(user.getAffiliation()).isEqualTo("Delft University of Technology");
    }

    @Test
    public void testGetRole() {
        Assertions.assertThat(user.getRole()).isEqualTo(UserRole.RESEARCHER);
    }

    @Test
    public void testIsAccountNonExpired() {
        Assertions.assertThat(user.isAccountNonExpired()).isTrue();
    }

    @Test
    public void testIsAccountNonLocked() {
        Assertions.assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    public void testIsCredentialsNonExpired() {
        Assertions.assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    public void testIsEnabled() {
        Assertions.assertThat(user.isEnabled()).isTrue();
    }

    @Test
    public void testSetPassword() {
        user.setPassword("thisisalsonotencrypted");
        Assertions.assertThat(user.getPassword()).isEqualTo("thisisalsonotencrypted");
    }

    @Test
    public void testSetFirstName() {
        user.setFirstName("Johnny");
        Assertions.assertThat(user.getFirstName()).isEqualTo("Johnny");
    }

    @Test
    public void testSetLastName() {
        user.setLastName("Roe");
        Assertions.assertThat(user.getLastName()).isEqualTo("Roe");
    }

    @Test
    public void testSetAffiliation() {
        user.setAffiliation("Eidgenössische Technische Hochschule Zürich");
        Assertions.assertThat(user.getAffiliation())
                .isEqualTo("Eidgenössische Technische Hochschule Zürich");
    }

    @Test
    public void testSetAccountNonExpired() {
        user.setAccountNonExpired(false);
        Assertions.assertThat(user.isAccountNonExpired()).isFalse();
    }

    @Test
    public void testSetAccountNonLocked() {
        user.setAccountNonLocked(false);
        Assertions.assertThat(user.isAccountNonLocked()).isFalse();
    }

    @Test
    public void testSetCredentialsNonExpired() {
        user.setCredentialsNonExpired(false);
        Assertions.assertThat(user.isCredentialsNonExpired()).isFalse();
    }

    @Test
    public void testSetEnabled() {
        user.setEnabled(false);
        Assertions.assertThat(user.isEnabled()).isFalse();
    }

    @Test
    public void testSetAuthorities() {
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
