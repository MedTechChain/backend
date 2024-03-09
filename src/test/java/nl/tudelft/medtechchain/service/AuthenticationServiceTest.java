package nl.tudelft.medtechchain.service;

import jakarta.persistence.EntityExistsException;
import java.util.List;
import java.util.UUID;

import nl.tudelft.medtechchain.TestConfig;
import nl.tudelft.medtechchain.model.Researcher;
import nl.tudelft.medtechchain.model.UserData;
import nl.tudelft.medtechchain.model.UserRole;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
public class AuthenticationServiceTest {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserDataRepository userDataRepository;

    @BeforeEach
    void setup() {
        List<UUID> researcherUserIds = this.userDataRepository
                .findAllResearchers().stream().map(Researcher::getUserId).toList();
        this.userDataRepository.deleteAllById(researcherUserIds);
    }

    @Test
    public void testRegisterNewUserEmailIsNull() {
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser(null,
                                "Jonh", "Doe", "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterNewUserFirstNameIsNull() {
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser("john.doe@tudelft.nl",
                                null, "Doe", "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterNewUserLastNameIsNull() {
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser("john.doe@tudelft.nl",
                                "John", null, "TU Delft"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRegisterNewUserEmailAlreadyExists() {
        this.userDataRepository.save(new UserData("jdoe", "somepassword", "john.doe@tudelft.nl",
                "John", "Doe", "TU Delft", UserRole.RESEARCHER));
        Assertions
                .assertThatThrownBy(() ->
                        this.authenticationService.registerNewUser("john.doe@tudelft.nl",
                                "John", "Doe", "TU Delft"))
                .isInstanceOf(EntityExistsException.class);
    }
}
