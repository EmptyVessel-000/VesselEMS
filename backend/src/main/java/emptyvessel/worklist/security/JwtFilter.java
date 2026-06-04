package emptyvessel.worklist.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import emptyvessel.worklist.model.UserRole;
import emptyvessel.worklist.repository.RoleRepository;
import emptyvessel.worklist.repository.UserRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public JwtFilter(JwtService jwtService, UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.jwtService = jwtService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.validateToken(token)) {
                Long userId = jwtService.getUserIdFromToken(token);

                List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
                List<SimpleGrantedAuthority> authorities = userRoles.stream()
                        .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                        .filter(r -> r != null)
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName().toUpperCase()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }
}