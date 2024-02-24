package nl.tudelft.healthblocks.service;

import nl.tudelft.healthblocks.entities.ResearcherDTO;
import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.repositories.UserDataRepository;
import nl.tudelft.healthblocks.entities.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A class that communicates with the database with the user data.
 */
@Service
public class AuthenticationService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * Creates an AuthenticationService object with the specified values.
     * In addition, it created the administrator user with the default username, password and userID.
     *
     * @param userDataRepository    the database to store users (i.e. user data)
     * @param passwordEncoder       the password encoder to hash user passwords
     */
    public AuthenticationService(UserDataRepository userDataRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${admin.username}") String adminUsername,
                                 @Value("${admin.password}") String adminPassword) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;

        // If the admin user does not exist, create one
        if (userDataRepository.findByUsername(adminUsername).isEmpty()) {
            UserData admin = new UserData(adminUsername, this.passwordEncoder.encode(adminPassword),
                    "admin@tudelft.nl", "Admin", "Admin", "TUDelft", UserRole.ADMIN);
            userDataRepository.save(admin);
        };

        //this.userDataRepository.save(new UserData("Jegor", this.passwordEncoder.encode("password1"),
        //        "jegor@tudelft.nl", "Jegor", "Zelenjak", "TUDelft", UserRole.RESEARCHER));
        //this.userDataRepository.save(new UserData("Alin", this.passwordEncoder.encode("password1"),
        //        "alin@tudelft.nl", "Alin", "Rosu", "TUDelft", UserRole.RESEARCHER));
    }

    /**
     * Retrieves the user with the given username.
     *
     * @param username      the username of the user that is requested
     * @return              the user (i.e. the UserData object) if they have been found in the database
     * @throws UsernameNotFoundException when the user with the given username has not been found in the database
     */
    @Override
    public UserData loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Could not find user with username: %s", username));
        }
        return user.get();
    }

    /**
     * Retrieves the user with the given userID.
     *
     * @param userId        the userID of the user that is requested
     * @return              the user (i.e. the UserData object) if they have been found in the database
     * @throws UsernameNotFoundException when the user with the given userID has not been found in the database
     */
    public UserData loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUserId(userId);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Could not find user with userId: %s", userId.toString()));
        }
        return user.get();
    }

    /**
     * Creates and registers a new user. By default, the user is assigned the "researcher" role.
     *
     * @return              true if the new user has been registered successfully, false if the user already exists
     */
    public boolean registerNewUser(String email, String firstName, String lastName, String affiliation) {
        String username = "alin";  // TODO: generate one
        if (this.userDataRepository.findByUsername(username).isPresent()) {
            return false;
        }

        String password = this.passwordEncoder.encode("123"); // TODO: generate a password
        UserData newUser = new UserData(username, password, email, firstName, lastName, affiliation, UserRole.RESEARCHER);
        this.userDataRepository.save(newUser);
        return true;
    }

    public List<ResearcherDTO> getAllResearchers() {
        return this.userDataRepository.findAllResearchers();
    }

    public void deleteUser() {
        // TODO: to be implemented
    }

    /**
     * Changes the password of the user with the given username.
     *
     * @param username      the username of the user whose password will be changed
     * @param newPassword   the new password for the specified user
     */
    public void changePassword(String username, String newPassword) {
        UserData user = loadUserByUsername(username);
        //user.setPassword(passwordEncoder.encode(newPassword));
        userDataRepository.save(user);
    }
}
