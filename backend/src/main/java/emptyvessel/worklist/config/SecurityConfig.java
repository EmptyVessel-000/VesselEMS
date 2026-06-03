package emptyvessel.worklist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(
                                                                org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                                "/",
                                                "/index",
                                                "/demo.html",
                                                "/login",
                                                "/register",
                                                "/403.html",
                                                "/**/*.js",
                                                "/**/*.css",
                                                "/**/*.png",
                                                "/**/*.jpg",
                                                "/**/*.jpeg",
                                                "/**/*.gif",
                                                "/**/*.svg",
                                                "/**/*.ico",
                                                "/api/auth/**")
                                .permitAll()
                                .requestMatchers("/manager.html").hasRole("MANAGER")
                                .requestMatchers("/member.html").hasAnyRole("MEMBER", "MANAGER")
                                .requestMatchers("/guest.html").hasAnyRole("GUEST", "MANAGER")
                                .requestMatchers("/api/users/**").authenticated()
                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex.accessDeniedPage("/403.html"))
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/index", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/login")
                                                .permitAll());
                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
