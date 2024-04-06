package nl.medtechchain.models;

/**
 * An enum class used to represent different user roles,
 *  used for authorization checks when accessing endpoints.
 * Currently, it only has "researcher" and "admin" roles.
 */
public enum UserRole {
    ADMIN,
    RESEARCHER;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
