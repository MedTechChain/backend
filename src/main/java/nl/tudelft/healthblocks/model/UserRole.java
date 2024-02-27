package nl.tudelft.healthblocks.model;

/**
 * An enum class used to represent different user roles.
 * Currently, it only has "Researcher" and "Admin" roles.
 * "Unknown" is only used locally when validating a JWT if the role is not "Researcher" or "Admin".
 */
public enum UserRole {
    ADMIN,
    RESEARCHER,
    UNKNOWN
}
