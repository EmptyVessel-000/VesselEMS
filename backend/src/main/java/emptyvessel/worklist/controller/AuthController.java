package emptyvessel.worklist.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emptyvessel.worklist.common.ApiResponse;
import emptyvessel.worklist.dto.LoginDto;
import emptyvessel.worklist.dto.RegisterDto;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.service.AuthService;
import emptyvessel.worklist.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

    public AuthController(AuthService authService,
            UserService userService,
            org.springframework.security.authentication.AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterDto request) {
        return ApiResponse.success(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ApiResponse<String> login(@Valid @RequestBody LoginDto request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {
        try {
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken
                    .unauthenticated(request.email(), request.password());
            var authentication = authenticationManager.authenticate(authRequest);

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            httpRequest.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext);

            return ApiResponse.success("Login successful");
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ApiResponse.error(401, "邮箱或密码错误");
        }
    }

    @GetMapping("/me")
    public ApiResponse<java.util.Map<String, Object>> getCurrentUser(java.security.Principal principal) {
        if (principal == null) {
            return ApiResponse.error(401, "未登录");
        }
        var user = userService.getUserByEmail(principal.getName()).orElseThrow();
        String roleName = user.getRole().name().replace("ROLE_", "").toLowerCase();
        return ApiResponse.success(java.util.Map.of(
                "id", user.getId(),
                "role", roleName));
    }
}