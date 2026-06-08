package vesselems.controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vesselems.common.ApiResponse;
import vesselems.dto.LoginDto;
import vesselems.dto.RegisterDto;
import vesselems.model.Role;
import vesselems.model.User;
import vesselems.repository.MenuPermissionRepository;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRepository;
import vesselems.repository.UserRoleRepository;
import vesselems.security.JwtService;
import vesselems.service.AuthService;
import vesselems.service.PermissionRoleService;
import vesselems.service.PermissionService;
import vesselems.service.RoleMenuService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final RoleRepository roleRepository;
        private final RoleMenuService roleMenuService;
        private final PermissionRoleService permissionRoleService;
        private final PermissionService permissionService;
        private final MenuPermissionRepository menuPermissionRepository;
        private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

        public AuthController(AuthService authService,
                        JwtService jwtService,
                        UserRepository userRepository,
                        UserRoleRepository userRoleRepository,
                        RoleRepository roleRepository,
                        RoleMenuService roleMenuService,
                        PermissionRoleService permissionRoleService,
                        PermissionService permissionService,
                        MenuPermissionRepository menuPermissionRepository,
                        org.springframework.security.authentication.AuthenticationManager authenticationManager) {
                this.authService = authService;
                this.jwtService = jwtService;
                this.userRepository = userRepository;
                this.userRoleRepository = userRoleRepository;
                this.roleRepository = roleRepository;
                this.roleMenuService = roleMenuService;
                this.permissionRoleService = permissionRoleService;
                this.permissionService = permissionService;
                this.menuPermissionRepository = menuPermissionRepository;
                this.authenticationManager = authenticationManager;
        }

        @PostMapping("/register")
        public ApiResponse<User> register(@Valid @RequestBody RegisterDto request) {
                return ApiResponse.success(authService.registerUser(request));
        }

        @PostMapping("/login")
        public ApiResponse<java.util.Map<String, Object>> login(@Valid @RequestBody LoginDto request) {
                authenticationManager.authenticate(
                                UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
                                                request.password()));

                User user = userRepository.findByEmail(request.email()).orElseThrow();

                if (user.getStatus() != null && user.getStatus() != 1) {
                        throw new RuntimeException("账户已被禁用");
                }

                String token = jwtService.generateToken(user.getId());

                List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                                .filter(r -> r != null)
                                .map(r -> r.getRoleName())
                                .collect(Collectors.toList());

                return ApiResponse.success(java.util.Map.of(
                                "token", token,
                                "user", java.util.Map.of(
                                                "id", user.getId(),
                                                "username", user.getUsername(),
                                                "roles", roles)));
        }

        @GetMapping("/me")
        public ApiResponse<java.util.Map<String, Object>> getCurrentUser(Authentication auth) {
                Long userId = (Long) auth.getPrincipal();
                User user = userRepository.findById(userId).orElseThrow();

                List<String> roles = userRoleRepository.findByUserId(userId).stream()
                                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                                .filter(r -> r != null)
                                .map(r -> r.getRoleName())
                                .collect(Collectors.toList());

                return ApiResponse.success(java.util.Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "roles", roles));
        }

        @GetMapping("/permissions")
        public ApiResponse<java.util.Map<String, Object>> getPermissions(Authentication auth) {
                Long userId = (Long) auth.getPrincipal();

                boolean isSuperAdmin = userRoleRepository.findByUserId(userId).stream()
                                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                                .filter(r -> r != null)
                                .anyMatch(r -> "super_admin".equals(r.getRoleName()));

                Set<Long> menuIds = new LinkedHashSet<>();
                Set<String> permCodes = new LinkedHashSet<>();

                if (isSuperAdmin) {
                        menuIds.add(-1L);
                        permCodes.add("*");
                } else {
                        Set<Long> permissionIds = new LinkedHashSet<>();
                        for (var ur : userRoleRepository.findByUserId(userId)) {
                                Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
                                if (role == null || (role.getStatus() != null && role.getStatus() != 1))
                                        continue;
                                roleMenuService.findByRoleId(ur.getRoleId())
                                                .forEach(rm -> menuIds.add(rm.getMenuId()));
                                permissionRoleService.findByRoleId(ur.getRoleId())
                                                .forEach(rp -> permissionIds.add(rp.getPermissionId()));
                        }

                        // 构建 menu_perm 反向索引：permissionId → 所属 menuId 集合
                        java.util.Map<Long, Set<Long>> permMenuMap = new java.util.LinkedHashMap<>();
                        for (var mp : menuPermissionRepository.findAll()) {
                                permMenuMap.computeIfAbsent(mp.getPermissionId(), k -> new LinkedHashSet<>())
                                                .add(mp.getMenuId());
                        }

                        for (Long permId : permissionIds) {
                                try {
                                        String code = permissionService.getPermissionById(permId).getPermissionCode();
                                        Set<Long> requiredMenus = permMenuMap.get(permId);
                                        if (requiredMenus == null || requiredMenus.isEmpty()) {
                                                // 无菜单关联的权限：直接放行
                                                permCodes.add(code);
                                        } else {
                                                // 有菜单关联的权限：必须拥有对应菜单才放行
                                                requiredMenus.retainAll(menuIds);
                                                if (!requiredMenus.isEmpty()) {
                                                        permCodes.add(code);
                                                }
                                        }
                                } catch (IllegalArgumentException ignored) {
                                }
                        }
                }

                return ApiResponse.success(java.util.Map.of(
                                "menus", menuIds,
                                "permissions", permCodes));
        }
}