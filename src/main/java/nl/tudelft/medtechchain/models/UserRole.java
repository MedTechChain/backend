package nl.tudelft.medtechchain.models;

/**
 * An enum class used to represent different user roles,
 *  used for authorization checks when accessing endpoints.
 * Currently, it only has "researcher" and "admin" roles.
 */
public enum UserRole {
    ADMIN("admin"),
    RESEARCHER("researcher");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
