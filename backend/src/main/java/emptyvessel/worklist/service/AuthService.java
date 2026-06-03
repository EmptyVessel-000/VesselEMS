package emptyvessel.worklist.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.dto.RegisterDto;
import emptyvessel.worklist.model.Role;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.model.UserRole;
import emptyvessel.worklist.repository.RoleRepository;
import emptyvessel.worklist.repository.UserRepository;
import emptyvessel.worklist.repository.UserRoleRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("邮箱已被使用: " + dto.email());
        }
        User user = new User();
        dto.apply(user);
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setStatus(1);
        user = userRepository.save(user);

        Role role = roleRepository.findByRoleName(dto.roleName())
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + dto.roleName()));
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleRepository.save(userRole);

        return user;
    }
}