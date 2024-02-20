package nl.tudelft.healthblocks.repositories;

import nl.tudelft.healthblocks.entities.UserData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A class for the database that stores the user data.
 */
@Repository
public interface UserDataRepository extends CrudRepository<UserData, String> {

    /**
     * Finds a user by their username.
     *
     * @param username the username of the user
     * @return the requested user (if it has been found)
     */
    Optional<UserData> findByUsername(String username);

    /**
     * Finds a user by their userID.
     *
     * @param userId the userID of the user
     * @return the requested user (if it has been found)
     */
    Optional<UserData> findByUserId(long userId);

}
