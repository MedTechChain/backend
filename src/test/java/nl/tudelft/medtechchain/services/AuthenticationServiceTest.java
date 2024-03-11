package nl.tudelft.medtechchain.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.medtechchain.models.Researcher;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql("/data.sql")
public class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        List<UUID> researcherUserIds = this.userDataRepository
                .findAllResearchers().stream().map(Researcher::getUserId).toList();
        this.userDataRepository.deleteAllById(researcherUserIds);
    }

    @Test
    public void testLoadUserByUsernameUserDoesNotExist() {
        Assertions.assertThatThrownBy(() -> this.authenticationService.loadUserByUsername("hjklkj"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void testLoadUserByUsernameUserExists() {
        this.userDataRepository.save(new UserData("jdoe", "somepassword",
                "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER));

        Assertions.assertThat(this.authenticationService
                        .loadUserByUsername("jdoe").getUsername()).isEqualTo("jdoe");

    }

    @Test
    public void testLoadUserByUserIdUserDoesNotExist() {
        UUID userId = UUID.fromString("9dedf9d3-a2c5-470e-86b8-5dfccdd0c2b0");

        Assertions.assertThatThrownBy(() -> this.authenticationService.loadUserByUserId(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testLoadUserByUserIdUserExists() {
        UserData user = this.userDataRepository.save(new UserData("jdoe", "somepassword",
                "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER));
        UUID userId = user.getUserId();

        Assertions.assertThat(this.authenticationService.loadUserByUserId(userId)).isEqualTo(user);
    }

    @Test
    public void testRegisterNewUserInvalidEmail() {
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser("J.Doe[at]tudelft.nl",
                                "John", "Doe", "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterNewUserEmailAlreadyExists() {
        this.userDataRepository.save(new UserData("jdoe", "somepassword", "J.Doe@tudelft.nl",
                "John", "Doe", "TU Delft", UserRole.RESEARCHER));
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser("J.Doe@tudelft.nl",
                                "John", "Doe", "TU Delft"))
                .isInstanceOf(EntityExistsException.class);
    }

    @Test
    public void testRegisterNewUserUsernameGeneration() {
        this.authenticationService.registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft");
        this.authenticationService.registerNewUser("J.Doe-1@tudelft.nl", "John", "Doe", "TU Delft");
        this.authenticationService.registerNewUser("J.Doe-2@tudelft.nl", "John", "Doe", "TU Delft");

        Assertions.assertThat(this.userDataRepository.findByUsername("jdoe")).isPresent();
        Assertions.assertThat(this.userDataRepository.findByUsername("jdoe1")).isPresent();
        Assertions.assertThat(this.userDataRepository.findByUsername("jdoe2")).isPresent();
    }

    @Test
    public void testRegisterNewUserFirstNameIsEmpty() {
        Assertions.assertThatThrownBy(() -> this.authenticationService
                        .registerNewUser("J.Doe@tudelft.nl", "", "Doe", "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterNewUserLastNameIsEmpty() {
        Assertions.assertThatThrownBy(() -> this.authenticationService
                        .registerNewUser("J.Doe@tudelft.nl", "John", "", "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testGetAllResearchers() {
        String firstName = "John";
        String lastName = "Doe";
        String email1 = "J.Doe@tudelft.nl";
        String email2 =  "J.Doe-1@tudelft.nl";
        String email3 = "J.Doe-2@tudelft.nl";
        String affiliation = "TU Delft";

        UUID userId1 = this.authenticationService
                .registerNewUser(email1, firstName, lastName, affiliation).getUserId();
        UUID userId2 = this.authenticationService
                .registerNewUser(email2, firstName, lastName, affiliation).getUserId();
        UUID userId3 = this.authenticationService
                .registerNewUser(email3, firstName, lastName, affiliation).getUserId();

        List<Researcher> expectedResearcherList = List.of(
                new Researcher(userId1, firstName, lastName, email1, affiliation),
                new Researcher(userId2, firstName, lastName, email2, affiliation),
                new Researcher(userId3, firstName, lastName, email3, affiliation)
        );
        Assertions.assertThatCollection(this.authenticationService.getAllResearchers())
                .hasSameElementsAs(expectedResearcherList);
    }

    @Test
    public void testUpdateUserUserDoesNotExist() {
        UUID userId = UUID.fromString("9dedf9d3-a2c5-470e-86b8-5dfccdd0c2b0");
        Assertions.assertThatThrownBy(() -> this.authenticationService
                        .updateUser(userId, "John", "Doe", "TU Delft"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testUpdateUser() {
        String oldFirstName = "John;";  // A small typo
        String newFirstName = "John";
        String oldLastName = "Doe\\";   // A small typo
        String newLastName = "Doe";
        String email = "J.Doe@tudelft.nl";
        String oldAffiliation = "TU Delft";
        String newAffiliation = "Delft University of Technology";

        UserData user = this.authenticationService
                .registerNewUser(email, oldFirstName, oldLastName, oldAffiliation);
        UserData updatedUser = this.authenticationService
                .updateUser(user.getUserId(), newFirstName, newLastName, newAffiliation);
        Assertions.assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
        Assertions.assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
        Assertions.assertThat(updatedUser.getAffiliation()).isEqualTo(newAffiliation);
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo(email);
    }

    @Test
    public void testDeleteUserUserDoesNotExist() {
        UUID userId = UUID.fromString("9dedf9d3-a2c5-470e-86b8-5dfccdd0c2b0");
        Assertions.assertThatThrownBy(() -> this.authenticationService.deleteUser(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testDeleteUserUserExists() {
        UUID userId = this.authenticationService
                .registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft").getUserId();
        this.authenticationService.deleteUser(userId);
        Assertions.assertThatThrownBy(() -> this.authenticationService.loadUserByUserId(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void testChangePasswordUserDoesNotExist() {
        Assertions.assertThatThrownBy(() ->
                    this.authenticationService.changePassword("jdoe", "password1", "password2"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    public void testChangePasswordWrongCredentials() {
        UserData user = new UserData("jdoe", "password1",
                "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER);
        user.setPassword(this.passwordEncoder.encode("password1"));
        this.userDataRepository.save(user);

        Assertions.assertThatThrownBy(() ->
                this.authenticationService.changePassword("jdoe", "123456", "password2"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    public void testChangePasswordSuccessful() {
        UserData user = new UserData("jdoe", "password1",
                "J.Doe@tudelft.nl", "John", "Doe", "TU Delft", UserRole.RESEARCHER);
        user.setPassword(this.passwordEncoder.encode("password1"));
        user = this.userDataRepository.save(user);

        this.authenticationService.changePassword("jdoe", "password1", "password2");

        UserData updatedUser = this.userDataRepository.findByUserId(user.getUserId()).get();
        Assertions.assertThat(updatedUser.getPassword()).isNotEqualTo(user.getPassword());
        Assertions.assertThat(this.passwordEncoder
                .matches("password2", updatedUser.getPassword())).isTrue();
    }

    @Test
    public void testEqualsTrue() {
        UserData user = this.authenticationService
                .registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft");
        Assertions.assertThat(user).isEqualTo(user);
    }

    @Test
    public void testEqualsFalse() {
        UserData user1 = this.authenticationService
                .registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft");
        UserData user2 = this.authenticationService
                .registerNewUser("J.Doe-1@tudelft.nl", "John", "Doe", "TU Delft");
        Assertions.assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void testHashCodeEquals() {
        UserData user = this.authenticationService
                .registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft");
        Assertions.assertThat(user.hashCode()).isEqualTo(user.hashCode());
    }

    @Test
    public void testHashCodeNotEquals() {
        UserData user1 = this.authenticationService
                .registerNewUser("J.Doe@tudelft.nl", "John", "Doe", "TU Delft");
        UserData user2 = this.authenticationService
                .registerNewUser("J.Doe-1@tudelft.nl", "John", "Doe", "TU Delft");
        Assertions.assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }
}
