package nl.tudelft.healthblocks.service;

import nl.tudelft.healthblocks.entities.UserData;
import nl.tudelft.healthblocks.repositories.UserDataRepository;
import nl.tudelft.healthblocks.security.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
                                 @Value("${admin.userid}") long userId,
                                 @Value("${admin.password}") String adminPassword) {
        this.userDataRepository = userDataRepository;
        this.passwordEncoder = passwordEncoder;

        // Check if the admin user already exists
        if (userDataRepository.findByUsername(adminUsername).isPresent()) return;

        // If the admin user does not exist, create one
        UserData admin = new UserData(adminUsername, userId, passwordEncoder.encode(adminPassword), UserRole.ADMIN);
        userDataRepository.save(admin);
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
    public UserData loadUserByUserId(long userId) throws UsernameNotFoundException {
        Optional<UserData> user = this.userDataRepository.findByUserId(userId);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(String.format("Could not find user with userId: %d", userId));
        }
        return user.get();
    }

    /**
     * Creates and registers a new user. By default, the user is assigned the "researcher" role.
     *
     * @param username      the username for the new user
     * @param userId        the userID for the new user
     * @param password      the password for the new user, which will be encoded using the (BCrypt) password encoder
     * @return              true if the new user has been registered successfully, false if the user already exists
     */
    public boolean registerNewUser(String username, long userId, String password) {
        // TODO: maybe add a check for userID
        if (userDataRepository.findByUsername(username).isPresent()) {
            return false;
        }

        UserData newUser = new UserData(username, userId, passwordEncoder.encode(password), UserRole.RESEARCHER);
        this.userDataRepository.save(newUser);
        return true;
    }

    /**
     * Changes the password of the user with the given username.
     *
     * @param username      the username of the user whose password will be changed
     * @param newPassword   the new password for the specified user
     */
    public void changePassword(String username, String newPassword) {
        UserData user = loadUserByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userDataRepository.save(user);
    }
}
