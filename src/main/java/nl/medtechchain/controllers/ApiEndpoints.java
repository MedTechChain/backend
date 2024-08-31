package nl.medtechchain.controllers;

import java.util.Set;

/**
 * A class that contains all supported API endpoints, so that they can be accessed from all places
 * (for easy reference and modification).
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
    public static final String READ_QUERIES_API = "/api/queries/read";
    public static final String CONFIGS_INTERFACE_API = "/api/configs/interface";
    public static final String CONFIGS_PLATFORM_API = "/api/configs/platform";
    public static final String CONFIGS_PLATFORM_UPDATE_API = "/api/configs/platform/update";

    // Prefixes for paths
    public static final String USERS_API_PREFIX = "/api/users";
    public static final String QUERIES_API_PREFIX = "/api/queries";
    public static final String CONFIGS_API_PREFIX = "/api/configs";

    // Actual endpoints (i.e. without the common prefix)
    public static final String LOGIN = "/login";
    public static final String REGISTER = "/register";
    public static final String GET_RESEARCHERS = "/researchers";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String CHANGE_PASSWORD = "/change_password";
    public static final String QUERIES = "";
    public static final String READ = "/read";
    public static final String INTERFACE = "/interface";
    public static final String PLATFORM = "/platform";
    public static final String PLATFORM_UPDATE = "/platform/update";


    // Paths that do not require JWT. For parts that are not in this list,
    //  401 Unauthorized will be returned if the JWT is missing.
    public static Set<String> NO_JWT_PATHS = Set.of(LOGIN_API, CHANGE_PASSWORD_API);

    /**
     * This class should not be instantiated.
     */
    private ApiEndpoints() {
    }
}
