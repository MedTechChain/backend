package nl.tudelft.healthblocks.service;

import jakarta.persistence.EntityExistsException;
import nl.tudelft.healthblocks.entities.ResearcherDTO;
import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.repositories.UserDataRepository;
import nl.tudelft.healthblocks.entities.UserRole;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A service class that communicates with the database with the user data.
 */
@Service
public class AuthenticationService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    private final PasswordEncoder passwordEncoder;

    private final Environment env;

    /**
     * Creates an AuthenticationService object with the specified values.
     * In addition, it creates the administrator user with the default details (if the admin does not exist).
     *
     * @param userDataRepository    the database to store the user data
     * @param passwordEncoder       the password encoder to hash user passwords
     * @param env                   Spring environment (used to get admin default details from application.properties)
     */
    public AuthenticationService(UserDataRepository userDataRepository, PasswordEncoder passwordEncoder, Environment env) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;

        // If the admin user does not exist, create one
        if (this.userDataRepository.findByUsername(env.getProperty("admin.username")).isEmpty()) {
            this.createAdmin();
        };
    }

    /**
     * Creates the administrator user with the default details.
     */
    private void createAdmin() {
        UserData admin = new UserData(
                this.env.getProperty("${admin.username}"),
                this.passwordEncoder.encode(this.env.getProperty("${admin.password}")),
                this.env.getProperty("${admin.email}"),
                this.env.getProperty("${admin.first-name}"),
                this.env.getProperty("${admin.last-name}"),
                this.env.getProperty("${admin.affiliation}"),
                UserRole.ADMIN
        );
        this.userDataRepository.save(admin);
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
     * @param userId        the userID (UUID) of the user that is requested
     * @return              the user (i.e. the UserData object) if they have been found in the database
     * @throws UsernameNotFoundException when the user with the given userID has not been found in the database
     */
    public UserData loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUserId(userId);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Could not find user with userID: %s", userId.toString()));
        }
        return user.get();
    }

    /**
     * Generates a unique username based on the provided first and last names of the user.
     * This username will be used to log in and will be sent to the user by email.
     * <p>
     * The username is created as follows: the first letter of the first name is prepended to the last name.
     * E.g. John Doe would get the username jdoe.
     * In case of ties, an integer is appended to the username.
     * E.g. the second John Doe would get the username jdoe-1, the third would get jdoe-2 etc.
     *
     * @param firstName     the first name of the user
     * @param lastName      the last name of the user
     * @return              the generated username
     */
    private String generateUsername(String firstName, String lastName) {
        // TODO: implement username generation
        return "Alin";
    }

    /**
     * Generates a password to log in. The password will be sent to the user by email.
     *
     * @return              the generated password
     */
    private String generatePassword() {
        // TODO: implement password generation
        return "password1";
    }

    /**
     * Creates and registers a new user. By default, the user is assigned the "researcher" role.
     * The generated username and password will be sent to the user by the provided email.
     *
     * @param email         the email of the new user; must be unique
     * @param firstName     the first name of the new user
     * @param lastName      the last name of the new user
     * @param affiliation   the affiliation of the new user
     */
    public void registerNewUser(String email, String firstName, String lastName, String affiliation) {
        // Check if the user with the email as the one provided already exists
        if (this.userDataRepository.findByEmail(email).isPresent()) {
            throw new EntityExistsException(String.format("User with email %s already exists", email));
        }

        String username = this.generateUsername(firstName, lastName);
        String password = this.generatePassword();
        String hashedPassword = this.passwordEncoder.encode(password);

        UserData newUser = new UserData(username, hashedPassword, email, firstName, lastName, affiliation, UserRole.RESEARCHER);
        this.userDataRepository.save(newUser);

        // TODO: send the username and password to the user by email
    }

    /**
     * Retrieves all researchers that are present in the database.
     * The information for each researcher includes userID, first name, last name, email and affiliation.
     *
     * @return              a list of retrieved researchers (with the information specified above)
     */
    public List<ResearcherDTO> getAllResearchers() {
        return this.userDataRepository.findAllResearchers();
    }

    /**
     * Deletes the user with the specified userID (UUID).
     *
     * @param userId        the userID of the user to be deleted
     */
    public void deleteUser(UUID userId) {
        if (this.userDataRepository.findByUserId(userId).isEmpty()) {
            throw new UsernameNotFoundException(String.format("User with userid %s not found", userId));
        }
        this.userDataRepository.deleteByUserId(userId);
    }

    /**
     * Changes the password of the user with the given username.
     * The provided old (current) password is compared to the actual old (current) password stored in the database.
     *
     * @param username      the username of the user whose password will be changed
     * @param oldPassword   the old (current) password of the specified user
     * @param newPassword   the new password for the specified user
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        UserData user = loadUserByUsername(username);
        if (!this.passwordEncoder.encode(oldPassword).equals(user.getPassword())) {
            throw new BadCredentialsException("Provided old password does not match the actual old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userDataRepository.save(user);
    }
}
