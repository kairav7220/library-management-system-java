package com.library.lms.config;

import com.library.lms.security.AuditLogoutHandler;
import com.library.lms.security.CompositePasswordEncoder;
import com.library.lms.security.CustomAccessDeniedHandler;
import com.library.lms.security.LoginEntryPoint;
import com.library.lms.security.LoginFailureHandler;
import com.library.lms.security.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   LoginSuccessHandler successHandler,
                                                   LoginFailureHandler failureHandler,
                                                   AuditLogoutHandler logoutHandler,
                                                   LoginEntryPoint entryPoint,
                                                   CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
        csrfRepository.setHeaderName("X-CSRFToken");
        http
            .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .addLogoutHandler(logoutHandler)
                .permitAll())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/js/**", "/styles/**", "/favicon.ico").permitAll()
                .requestMatchers("/logs").hasRole("Admin")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CompositePasswordEncoder();
    }
}
