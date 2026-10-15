package com.shani.caradvisor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Who may do what.
 * - Transport rep screen and API: login required (role REP). Answers are recorded with the rep's name.
 * - Employee API: open, addressed by an unguessable random case id. A pilot would put company SSO in front of it.
 * - CSRF is off because the API only accepts JSON: a cross-site JSON request needs a CORS preflight, and CORS is not enabled.
 *   The one upload (the payslip) must carry a custom header, which needs the same preflight, and it changes nothing.
 * - CSP allows scripts only from this server, so injected text can never run as code.
 */
@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    static final String CONTENT_SECURITY_POLICY = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/rep/**", "/rep.html").hasRole("REP")
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /** The rep's password comes from ADVISOR_REP_PASSWORD. Without it a random one is generated for this run only. */
    @Bean
    UserDetailsService reps(@Value("${advisor.rep.username:rep}") String username,
                            @Value("${advisor.rep.password:}") String password,
                            PasswordEncoder encoder) {
        String effective = password;
        if (effective == null || effective.isBlank()) {
            byte[] random = new byte[9];
            new SecureRandom().nextBytes(random);
            effective = HexFormat.of().formatHex(random);
            log.warn("ADVISOR_REP_PASSWORD is not set. Rep login for this run only: user '{}', password '{}'", username, effective);
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(username).password(encoder.encode(effective)).roles("REP").build());
    }
}
