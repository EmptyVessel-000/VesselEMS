package vesselems.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.dto.LoginDto;
import vesselems.dto.RegisterDto;
import vesselems.model.User;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRepository;
import vesselems.repository.UserRoleRepository;
import vesselems.security.JwtService;
import vesselems.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

        private final AuthService authService;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final RoleRepository roleRepository;
        private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

        public AuthController(AuthService authService,
                        JwtService jwtService,
                        UserRepository userRepository,
                        UserRoleRepository userRoleRepository,
                        RoleRepository roleRepository,
                        org.springframework.security.authentication.AuthenticationManager authenticationManager) {
                this.authService = authService;
                this.jwtService = jwtService;
                this.userRepository = userRepository;
                this.userRoleRepository = userRoleRepository;
                this.roleRepository = roleRepository;
                this.authenticationManager = authenticationManager;
        }

        @PostMapping("/register")
        public ApiResponse<User> register(@Valid @RequestBody RegisterDto request) {
                return ApiResponse.success(authService.registerUser(request));
        }

        @PostMapping("/login")
        public ApiResponse<java.util.Map<String, Object>> login(@Valid @RequestBody LoginDto request) {
                Authentication auth = authenticationManager.authenticate(
                                UsernamePasswordAuthenticationToken.unauthenticated(request.email(),
                                                request.password()));

                User user = userRepository.findByEmail(request.email()).orElseThrow();
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
}