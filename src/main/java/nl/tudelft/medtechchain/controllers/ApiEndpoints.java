package nl.tudelft.medtechchain.controllers;

/**
 * A class that contains all supported API endpoints, so that they can be accessed from all places
 *  (for easy reference and modification).
 */
public class ApiEndpoints {
    // Full paths
    public static final String LOGIN_API = "/api/users/login";
    public static final String REGISTER_API = "/api/users/register";
    public static final String GET_RESEARCHERS_API = "/api/users/researchers";
    public static final String UPDATE_API = "/api/users/update";
    public static final String DELETE_API = "/api/users/delete";
    public static final String CHANGE_PASSWORD_API = "/api/users/change_password";
    public static final String QUERIES_API = "/api/queries";

    // Prefixes for paths
    public static final String USERS_API_PREFIX = "/api/users";
    public static final String QUERIES_API_PREFIX = "/api/queries";

    // Actual endpoints (i.e. without the common prefix)
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String GET_RESEARCHERS = "/researchers";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String CHANGE_PASSWORD = "/change_password";
    public static final String QUERIES = "";
}