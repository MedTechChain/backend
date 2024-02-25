package nl.tudelft.healthblocks.service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.tudelft.healthblocks.entities.ResearcherDto;
import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.entities.UserRole;
import nl.tudelft.healthblocks.repositories.UserDataRepository;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * A service class that communicates with the database with the user data.
 */
@Service
@Transactional
public class AuthenticationService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final Environment env;

    /**
     * Creates an AuthenticationService object with the specified values.
     * In addition, it creates the admin user with the default details (if they do not exist).
     *
     * @param userDataRepository    the database to store the user data
     * @param passwordEncoder       the password encoder to hash user passwords
     * @param env                   the Spring environment (used to get admin default details)
     */
    public AuthenticationService(UserDataRepository userDataRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService,
                                 Environment env) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.env = env;

        // If the admin user does not exist, create one
        if (this.userDataRepository.findByUsername(env.getProperty("admin.username")).isEmpty()) {
            this.createAdmin();
        }
    }

    /**
     * Creates the administrator user with the default details.
     */
    private void createAdmin() {
        UserData admin = new UserData(
                this.env.getProperty("admin.username"),
                this.passwordEncoder.encode(this.env.getProperty("admin.password")),
                this.env.getProperty("admin.email"),
                this.env.getProperty("admin.first-name"),
                this.env.getProperty("admin.last-name"),
                this.env.getProperty("admin.affiliation"),
                UserRole.ADMIN
        );
        this.userDataRepository.save(admin);
    }

    /**
     * Retrieves the user with the given username.
     *
     * @param username                      the username of the user that is requested
     * @return                              the user (data) if they have been found in the database
     * @throws UsernameNotFoundException    when the user with the given username has not been found
     */
    @Override
    public UserData loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(
                    String.format("Could not find user with the username %s", username)
            );
        }
        return user.get();
    }

    /**
     * Retrieves the user with the given userID.
     *
     * @param userId                        the userID (UUID) of the user that is requested
     * @return                              the user (data) if they have been found in the database
     * @throws UsernameNotFoundException    when the user with the given userID has not been found
     */
    public UserData loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUserId(userId);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(
                    String.format("Could not find user with the userID %s", userId.toString())
            );
        }
        return user.get();
    }

    /**
     * Generates a unique username based on the provided first and last names of the user.
     * This username will be used to log in and will be sent to the user by email.
     * <br>
     * The username is created as follows:
     *   the first letter of the first name is prepended to the last name.
     * E.g. John Doe would get the username jdoe.
     * In case of ties, a number is appended to the username.
     * E.g. the second John Doe would get the username jdoe1, the third would get jdoe2 etc.
     *
     * @param firstName     the first name of the user
     * @param lastName      the last name of the user
     * @return              the generated username
     */
    private String generateUsername(String firstName, String lastName) {
        if (firstName.isEmpty() || lastName.isEmpty()) {
            throw new DataIntegrityViolationException("Empty first or last names");
        }

        String base = (firstName.charAt(0) + lastName).toLowerCase();
        List<String> similarUsernames = this.userDataRepository.findAllUsernamesByPrefix(base);
        if (similarUsernames.isEmpty()) {
            return base;
        }

        // The returned usernames are already sorted (ascending)
        String largestSuffix = similarUsernames.getLast().replaceFirst("^" + base, "");
        if (largestSuffix.isEmpty()) {
            return base + "1";
        }
        long largestSuffixNumber = Long.parseLong(largestSuffix);
        return base + (largestSuffixNumber + 1);
    }


    /**
     * Generates a random 128-ASCII-character password to log in.
     * The password will be sent to the user by email.
     * The idea has been taken from <a href="https://www.baeldung.com/java-generate-secure-password#using-custom-utility">Baeldung</a>
     *
     * @return              the generated password (consisting of 128 ASCII characters)
     */
    private String generatePassword() {
        Random random = new SecureRandom();
        IntStream randomAsciiCodes = random.ints(128, 33, 127);
        return randomAsciiCodes
                .mapToObj(code -> String.valueOf((char) code))
                .collect(Collectors.joining());
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
    public void registerNewUser(String email, String firstName,
                                String lastName, String affiliation) {
        // Check if the user with the email as the one provided already exists
        if (this.userDataRepository.findByEmail(email).isPresent()) {
            throw new EntityExistsException(
                    String.format("User with the email %s already exists", email)
            );
        }

        String username = this.generateUsername(firstName, lastName);
        String password = this.generatePassword();
        String hashedPassword = this.passwordEncoder.encode(password);

        UserData newUser = new UserData(username, hashedPassword, email,
                firstName, lastName, affiliation, UserRole.RESEARCHER);
        this.userDataRepository.save(newUser);

        // TODO: replace with some sort of template
        String emailContent = String.format("Dear %s,\n\n", firstName)
                + "You have been registered in HealthBlocks. Your default credentials are:\n\n"
                + String.format("\tUsername: %s\n\tPassword: %s\n\n", username, password)
                + "Make sure to change your password, and don't forget to make it secure.\n"
                + "Suggestion: you can also use a password manager.\n\n"
                + String.format("Should you have any questions, please contact %s.\n\n",
                    this.env.getProperty("admin.email"))
                + "Best regards,\nHealthBlocks";
        this.emailService.sendSimpleEmail(email, "Welcome to HealthBlocks", emailContent);
    }

    /**
     * Retrieves all researchers that are present in the database.
     * The information for each researcher is: userID, first name, last name, email and affiliation.
     *
     * @return              a list of retrieved researchers (with the information specified above)
     */
    public List<ResearcherDto> getAllResearchers() {
        return this.userDataRepository.findAllResearchers();
    }

    /**
     * Deletes the user with the specified userID (UUID).
     *
     * @param userId        the userID of the user to be deleted
     */
    public void deleteUser(UUID userId) {
        if (this.userDataRepository.findByUserId(userId).isEmpty()) {
            throw new UsernameNotFoundException(
                    String.format("User with userid %s not found", userId)
            );
        }
        this.userDataRepository.deleteByUserId(userId);
    }

    /**
     * Changes the password of the user with the given username.
     * The provided old (current) password is compared to the stored old (current) password.
     * TODO: implement the endpoint in the controller
     *
     * @param username      the username of the user whose password will be changed
     * @param oldPassword   the old (current) password of the specified user
     * @param newPassword   the new password for the specified user
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        UserData user = loadUserByUsername(username);
        if (!this.passwordEncoder.encode(oldPassword).equals(user.getPassword())) {
            throw new BadCredentialsException("Provided old password does not match the actual");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userDataRepository.save(user);
    }
}
