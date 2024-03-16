package nl.tudelft.medtechchain.config;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import nl.tudelft.medtechchain.jwt.JwtAuthenticationFilter;
import nl.tudelft.medtechchain.jwt.JwtProvider;
import nl.tudelft.medtechchain.models.UserRole;
import nl.tudelft.medtechchain.services.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * A configuration class for some of the Spring Security components.
 * Namely, it creates beans for SecurityFilterChain, AuthenticationProvider
 *  and AuthenticationManager, and it also creates the CorsConfigurationSource bean.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationService authenticationService;

    /**
     * Creates the SecurityFilterChain bean, needed for Spring Security.
     * Sets the permissions for different endpoints, adds custom filters (JwtAuthenticationFilter),
     *  and adds the custom login page (see <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html">Form Login</a>).
     * Other configurations are also applied (such as CORS, CSRF and session management).
     *
     * @param http              allows configuring Web-based security for HTTP requests
     * @param env               the Spring environment (to access the defined properties)
     * @return                  the created and configured SecurityFilterChain
     * @throws Exception        if something goes wrong during the SecurityFilterChain building
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, Environment env) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource(env)))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // For all endpoints except /api/users/login and /api/users/change_password,
                        //  401 will be returned if the JWT token is missing
                        //  (see JwtAuthenticationProvider for details)
                        .requestMatchers(HttpMethod.POST, "/api/users/login")
                            .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register")
                            .hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/users/researchers")
                            .hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/users/update")
                            .hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/users/delete")
                            .hasAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/api/users/change_password")
                            .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/queries")
                            .hasAuthority(UserRole.RESEARCHER.name())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(this.authenticationProvider())
                .addFilterBefore(
                        new JwtAuthenticationFilter(this.authenticationService, this.jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    /**
     * Creates the AuthenticationProvider bean, needed for Spring Security.
     *
     * @return                  the created and configured AuthenticationProvider bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(this.authenticationService);
        authenticationProvider.setPasswordEncoder(this.passwordEncoder);
        return authenticationProvider;
    }

    /**
     * Creates the AuthenticationProvider bean, needed for Spring Security.
     *
     * @param configuration     the configuration for the authentication
     * @return                  the created AuthenticationManager bean
     * @throws Exception        if something goes wrong when getting the AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Creates the CorsConfigurationSource bean, used in the Spring framework.
     * CORS (Cross-Origin Resource Sharing) lets us specify what kind of cross-domain
     *  requests are authorized (see: <a href="https://docs.spring.io/spring-framework/reference/web/webflux-cors.html#webflux-cors-intro">CORS</a>).
     * The configuration properties are taken from the application.properties file.
     *
     * @param env               the Spring environment (to access the defined properties)
     * @return                  the created and configured CorsConfigurationSource bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(Environment env) {
        String[] allowedOrigins = env.getProperty("spring.graphql.cors.allowed-origins", String[].class, new String[]{});
        String[] allowedHeaders = env.getProperty("spring.graphql.cors.allowed-headers", String[].class, new String[]{});
        String[] exposedHeaders = env.getProperty("spring.graphql.cors.exposed-headers", String[].class, new String[]{});
        String[] allowedMethods = env.getProperty("spring.graphql.cors.allowed-methods", String[].class, new String[]{});
        Boolean allowCredentials = env.getProperty("spring.graphql.cors.allow-credentials", Boolean.class, false);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setExposedHeaders(Arrays.asList(exposedHeaders));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowCredentials(allowCredentials);

        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
