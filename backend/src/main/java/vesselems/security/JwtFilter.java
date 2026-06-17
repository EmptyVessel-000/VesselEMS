package vesselems.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vesselems.model.Permission;
import vesselems.model.UserRole;
import vesselems.repository.PermissionRepository;
import vesselems.repository.PermissionRoleRepository;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRoleRepository;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PermissionRoleRepository permissionRoleRepository;
    private final PermissionRepository permissionRepository;

    public JwtFilter(JwtService jwtService, UserRoleRepository userRoleRepository, RoleRepository roleRepository,
            PermissionRoleRepository permissionRoleRepository, PermissionRepository permissionRepository) {
        this.jwtService = jwtService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.permissionRoleRepository = permissionRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 公开路径不验证token
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供有效的认证令牌\"}");
            return;
        }

        String token = header.substring(7);

        if (!jwtService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌已过期或无效\"}");
            return;
        }

        Long userId = jwtService.getUserIdFromToken(token);

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();

        boolean isSuperAdmin = false;
        for (UserRole ur : userRoles) {
            var role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role == null)
                continue;
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName().toUpperCase()));
            if ("super_admin".equals(role.getRoleName())) {
                isSuperAdmin = true;
            }
        }

        if (isSuperAdmin) {
            // 超级管理员拥有所有权限
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            // 加载所有权限标识，使 @PreAuthorize 检查通过
            permissionRepository.findAll()
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getPermissionCode())));
        } else {
            // 加载用户的权限标识
            for (UserRole ur : userRoles) {
                List<Long> permIds = permissionRoleRepository.findByRoleId(ur.getRoleId()).stream()
                        .map(pr -> pr.getPermissionId())
                        .collect(Collectors.toList());
                for (Long permId : permIds) {
                    Permission perm = permissionRepository.findById(permId).orElse(null);
                    if (perm != null) {
                        authorities.add(new SimpleGrantedAuthority(perm.getPermissionCode()));
                    }
                }
            }
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
                new ArrayList<>(authorities));
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}