package nl.tudelft.medtechchain.models;

/**
 * An enum class used to represent different user roles,
 *  used for authorization checks when accessing endpoints.
 * Currently, it only has "Researcher" and "Admin" roles.
 */
public enum UserRole {
    ADMIN,
    RESEARCHER
}
