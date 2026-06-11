package vesselems.controller;

import java.util.List;
import java.util.Map;

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
import vesselems.model.User;
import vesselems.security.JwtService;
import vesselems.service.AuthService;
import vesselems.service.PermissionService;
import vesselems.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final JwtService jwtService;
        private final UserService userService;
        private final PermissionService permissionService;
        private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

        public AuthController(AuthService authService,
                        JwtService jwtService,
                        UserService userService,
                        PermissionService permissionService,
                        org.springframework.security.authentication.AuthenticationManager authenticationManager) {
                this.authService = authService;
                this.jwtService = jwtService;
                this.userService = userService;
                this.permissionService = permissionService;
                this.authenticationManager = authenticationManager;
        }

        @PostMapping("/register")
        public ApiResponse<User> register(@Valid @RequestBody RegisterDto request) {
                return ApiResponse.success(authService.registerUser(request));
        }

        @PostMapping("/login")
        public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginDto request) {
                authenticationManager.authenticate(
                                UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
                                                request.password()));

                User user = userService.getUserByEmail(request.email())
                                .orElseThrow(() -> new RuntimeException("用户不存在"));

                if (user.getStatus() != null && user.getStatus() != 1) {
                        throw new RuntimeException("账户已被禁用");
                }

                String token = jwtService.generateToken(user.getId());

                List<String> roles = userService.listUsersWithRoles().stream()
                                .filter(dto -> dto.getId().equals(user.getId()))
                                .flatMap(dto -> dto.getRoleNames().stream())
                                .toList();

                return ApiResponse.success(Map.of(
                                "token", token,
                                "user", Map.of(
                                                "id", user.getId(),
                                                "username", user.getUsername(),
                                                "roles", roles)));
        }

        @GetMapping("/me")
        public ApiResponse<Map<String, Object>> getCurrentUser(Authentication auth) {
                Long userId = (Long) auth.getPrincipal();
                User user = userService.getUserById(userId)
                                .orElseThrow(() -> new RuntimeException("用户不存在"));

                List<String> roles = userService.listUsersWithRoles().stream()
                                .filter(dto -> dto.getId().equals(userId))
                                .flatMap(dto -> dto.getRoleNames().stream())
                                .toList();

                return ApiResponse.success(Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "roles", roles));
        }

        @GetMapping("/permissions")
        public ApiResponse<Map<String, Object>> getPermissions(Authentication auth) {
                Long userId = (Long) auth.getPrincipal();
                return ApiResponse.success(permissionService.calculateUserPermissions(userId));
        }
}