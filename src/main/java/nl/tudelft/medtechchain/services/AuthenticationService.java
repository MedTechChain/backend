package nl.tudelft.medtechchain.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import nl.tudelft.medtechchain.models.Researcher;
import nl.tudelft.medtechchain.models.UserData;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.repositories.UserDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * A service class that communicates with the database with the user data.
 */
@Service
@Transactional
@AllArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

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
            String message = String.format("Could not find user with the username %s", username);
            throw new UsernameNotFoundException(message);
        }
        return user.get();
    }

    /**
     * Retrieves the user with the given userID.
     *
     * @param userId                        the userID (UUID) of the user that is requested
     * @return                              the user (data) if they have been found in the database
     * @throws EntityNotFoundException      when the user with the given userID has not been found
     */
    public UserData loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUserId(userId);
        if (user.isEmpty()) {
            String message = String.format("Could not find user with userID %s", userId.toString());
            throw new EntityNotFoundException(message);
        }
        return user.get();
    }

    /**
     * Checks whether the provided email address is valid (i.e. matches the regex).
     * The regex is taken from <a href="https://www.baeldung.com/java-email-validation-regex">Baeldung</a>.
     *
     * @param emailAddress      the email address to validate
     * @return                  true if the provided email address is valid, false otherwise
     */
    private boolean isEmailValid(String emailAddress) {
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(emailRegex).matcher(emailAddress).matches();
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
     * @param firstName                     the first name of the user
     * @param lastName                      the last name of the user
     * @return                              the generated username
     * @throws DataIntegrityViolationException  if first and/or last names are empty
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
     * @return                              the generated password (128 ASCII characters)
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
     * @param email                         the email of the new user; must be unique
     * @param firstName                     the first name of the new user
     * @param lastName                      the last name of the new user
     * @param affiliation                   the affiliation of the new user
     * @throws DataIntegrityViolationException  if the email is invalid, or the name is empty
     * @throws EntityExistsException            if a user with the specified email already exists
     */
    public UserData registerNewUser(String email, String firstName,
                                    String lastName, String affiliation) {
        if (!isEmailValid(email)) {
            throw new DataIntegrityViolationException("Email address is not valid");
        }

        if (this.userDataRepository.findByEmail(email).isPresent()) {
            String message = String.format("User with email %s already exists", email);
            throw new EntityExistsException(message);
        }

        String username = this.generateUsername(firstName, lastName);
        String password = this.generatePassword();
        String hashedPassword = this.passwordEncoder.encode(password);

        UserData newUser = new UserData(username, hashedPassword, email,
                firstName, lastName, affiliation, UserRole.RESEARCHER);
        this.userDataRepository.save(newUser);

        // TODO: replace with some sort of template
        String emailContent = String.format("Dear %s,\n\n", firstName)
                + "You have been registered in MedTech Chain. Your default credentials are:\n\n"
                + String.format("\tUsername: %s\n\tPassword: %s\n\n", username, password)
                + "Make sure to change your password, and don't forget to make it secure.\n"
                + "Suggestion: you can also use a password manager.\n\n"
                + "Best regards,\nMedTech Chain";
        this.emailService.sendSimpleEmail(email, "Welcome to MedTech Chain", emailContent);
        return newUser;
    }

    /**
     * Retrieves all researchers that are present in the database.
     * The information for each researcher is: userID, first name, last name, email and affiliation.
     *
     * @return              a list of retrieved researchers (with the information specified above)
     */
    public List<Researcher> getAllResearchers() {
        return this.userDataRepository.findAllResearchers();
    }

    /**
     * Updates the personal details of the user.
     * The personal details that are updated are first name, last name and affiliation.
     *
     * @param userId        the userID (UUID) of the user to be updated
     * @param firstName     the first name of the user to be updated
     * @param lastName      the last name of the user to be updated
     * @param affiliation   the affiliation of the user to be updated
     */
    public UserData updateUser(UUID userId, String firstName, String lastName, String affiliation) {
        UserData user = this.loadUserByUserId(userId);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAffiliation(affiliation);

        return this.userDataRepository.save(user);
    }

    /**
     * Deletes the user with the specified userID (UUID).
     *
     * @param userId        the userID of the user to be deleted
     */
    public void deleteUser(UUID userId) {
        // Check if the user with the specified userId exists
        loadUserByUserId(userId);
        this.userDataRepository.deleteByUserId(userId);
    }
}
