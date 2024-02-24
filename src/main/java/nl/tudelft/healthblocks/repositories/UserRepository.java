package nl.tudelft.healthblocks.repositories;

import nl.tudelft.healthblocks.entities.ResearcherDTO;
import nl.tudelft.healthblocks.entities.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A class for the database that stores the user data.
 */
@Repository
public interface UserRepository extends JpaRepository<UserData, UUID> {

    /**
     * Finds a user by their username.
     *
     * @param username  the username of the user
     * @return          the requested user (if they have been found)
     */
    Optional<UserData> findByUsername(String username);

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
     * Finds all researchers (i.e. users with role "researcher"). Note that `UserRole.RESEARCHER = 1`.
     *
     * @return          a list of found researchers (their userID, first name, last name and affiliation)
     */
    @Query("SELECT new nl.tudelft.healthblocks.entities.ResearcherDTO(userId, firstName, lastName, affiliation) FROM UserData where role = 1")
    List<ResearcherDTO> findAllResearchers();

    /**
     * Deletes a user with the specified userID.
     *
     * @param userId    the userID of a user to be deleted
     * @return          the user that has been deleted
     */
    UserData deleteByUserId(UUID userId);

}
