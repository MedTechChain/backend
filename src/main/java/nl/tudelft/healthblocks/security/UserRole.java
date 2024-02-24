package nl.tudelft.healthblocks.security;

/**
 * An enum class used to represent different user roles.
 * Currently, it only has "Researcher" and "Admin" roles.
 * "Unknown" is used locally, in case the role is not "Researcher" or "Admin".
 */
public enum UserRole {
    RESEARCHER,
    ADMIN,
    UNKNOWN,
}
