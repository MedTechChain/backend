package nl.tudelft.healthblocks.config;

import lombok.RequiredArgsConstructor;
import nl.tudelft.healthblocks.jwt.JwtAuthenticationFilter;
import nl.tudelft.healthblocks.jwt.JwtProvider;
import nl.tudelft.healthblocks.service.AuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationService authenticationService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // TODO: check what is needed
                        .requestMatchers(HttpMethod.POST, "api/auth/register").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/auth/change_password").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/change_role").hasAnyAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/delete").hasAnyAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(this.authenticationProvider())
                .addFilterBefore(
                        new JwtAuthenticationFilter(this.jwtProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                //.formLogin(Customizer.withDefaults())
                //.httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(this.passwordEncoder);
        authenticationProvider.setUserDetailsService(this.authenticationService);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
