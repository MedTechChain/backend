package nl.tudelft.healthblocks.config;

import lombok.RequiredArgsConstructor;
import nl.tudelft.healthblocks.entities.UserRole;
import nl.tudelft.healthblocks.jwt.JwtAuthenticationFilter;
import nl.tudelft.healthblocks.jwt.JwtProvider;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * A configuration class for some of the Spring Security components.
 * Namely, it creates beans for SecurityFilterChain, AuthenticationProvider and AuthenticationManager.
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
     *
     * @param http              the object that allows configuring Web-based security for HTTP requests
     * @return                  the created and configured SecurityFilterChain
     * @throws Exception        if something goes wrong during the SecurityFilterChain building
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users/login")
                            .permitAll()
                        .requestMatchers(HttpMethod.POST, "api/users/register")
                            .hasAnyAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/api/users/researchers")
                            .hasAnyAuthority(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/users/delete")
                            .hasAnyAuthority(UserRole.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(this.authenticationProvider())
                .addFilterBefore(
                        new JwtAuthenticationFilter(this.authenticationService, this.jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .formLogin(Customizer.withDefaults())
                .build();
    }

    /**
     * Creates the AuthenticationProvider bean, needed for Spring Security.
     *
     * @return                  the created AuthenticationProvider bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(this.passwordEncoder);
        authenticationProvider.setUserDetailsService(this.authenticationService);
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
}
