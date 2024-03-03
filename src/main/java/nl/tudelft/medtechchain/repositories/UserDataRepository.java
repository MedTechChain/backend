package nl.tudelft.medtechchain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.tudelft.medtechchain.model.Researcher;
import nl.tudelft.medtechchain.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


/**
 * A class for the database that stores the user data.
 */
@Repository
public interface UserDataRepository extends JpaRepository<UserData, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username  the username of the user
     * @return          the requested user (if they have been found)
     */
    Optional<UserData> findByUsername(String username);


    /**
     * Finds all usernames that start with the same prefix as the one provided.
     * This is used for username generation, in order to append a number
     *   in case of multiple users with the same base username.
     * E.g. if a new user John Doe is being registered, then 'jdoe' is the base username, and
     *   jdoe1, jdoe2, ..., jdoe`n` will be retrieved.
     *
     * @param prefix    the prefix that a username should start with
     * @return          a list of found usernames
     */
    @Query("SELECT username FROM UserData WHERE role = 1 "
            + "AND username ILIKE :prefix||'%' ORDER BY username ASC ")
    List<String> findAllUsernamesByPrefix(String prefix);

    /**
     * Finds a user by their userID.
     *
     * @param userId    the userID of the user
     * @return          the requested user (if they have been found)
     */
    Optional<UserData> findByUserId(UUID userId);

    /**
     * Finds a user by their email.
     *
     * @param email     the email of the user
     * @return          the requested user (if they have been found)
     */
    Optional<UserData> findByEmail(String email);


    /**
     * Finds all researchers (users with role "researcher"). Note that `UserRole.RESEARCHER = 1`.
     *
     * @return          a list of found researchers
     *                  (their userID, first name, last name, email and affiliation)
     */
    @Query("SELECT new nl.tudelft.medtechchain.model."
            + "Researcher(userId, firstName, lastName, email, affiliation) "
            + "FROM UserData WHERE role = 1")
    List<Researcher> findAllResearchers();

    /**
     * Deletes a user with the specified userID.
     *
     * @param userId    the userID of a user to be deleted
     */
    void deleteByUserId(UUID userId);

}
