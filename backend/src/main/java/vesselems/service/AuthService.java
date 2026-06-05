package vesselems.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vesselems.dto.RegisterDto;
import vesselems.model.Role;
import vesselems.model.User;
import vesselems.model.UserRole;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRepository;
import vesselems.repository.UserRoleRepository;

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
        user.setCreateTime(LocalDateTime.now());
        user.setModifyTime(LocalDateTime.now());
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