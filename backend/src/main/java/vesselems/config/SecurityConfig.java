package vesselems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import vesselems.security.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtFilter jwtFilter;

        public SecurityConfig(JwtFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                // 公开接口
                                                .requestMatchers("/api/auth/**").permitAll()

                                                // ===== 用户管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/users/**")
                                                .hasAuthority("user:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/users/**")
                                                .hasAuthority("user:edit")
                                                .requestMatchers(HttpMethod.PATCH, "/api/users/**")
                                                .hasAuthority("user:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/users/**")
                                                .hasAuthority("user:delete")

                                                // ===== 角色管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/roles/**")
                                                .hasAuthority("role:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/roles/**")
                                                .hasAuthority("role:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/roles/**")
                                                .hasAuthority("role:delete")

                                                // ===== 系统配置 =====
                                                .requestMatchers(HttpMethod.POST, "/api/configs/**")
                                                .hasAuthority("config:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/configs/**")
                                                .hasAuthority("config:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/configs/**")
                                                .hasAuthority("config:delete")

                                                // ===== 菜单管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/menus/**")
                                                .hasAuthority("menu:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/menus/**")
                                                .hasAuthority("menu:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/menus/**")
                                                .hasAuthority("menu:delete")

                                                // ===== 权限管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/permissions/**")
                                                .hasAuthority("perm:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/permissions/**")
                                                .hasAuthority("perm:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/permissions/**")
                                                .hasAuthority("perm:delete")

                                                // ===== 部门管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/departments/**")
                                                .hasAuthority("dept:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/departments/**")
                                                .hasAuthority("dept:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/departments/**")
                                                .hasAuthority("dept:delete")

                                                // ===== 数据源管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/ds/**")
                                                .hasAuthority("ds:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/ds/**").hasAuthority("ds:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/ds/**")
                                                .hasAuthority("ds:delete")

                                                // ===== 模型管理 =====
                                                .requestMatchers(HttpMethod.POST, "/api/model/**")
                                                .hasAuthority("model:create")
                                                .requestMatchers(HttpMethod.PUT, "/api/model/**")
                                                .hasAuthority("model:edit")
                                                .requestMatchers(HttpMethod.DELETE, "/api/model/**")
                                                .hasAuthority("model:delete")

                                                // 其他所有请求需要认证
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

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