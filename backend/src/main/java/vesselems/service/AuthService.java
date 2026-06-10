package vesselems.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vesselems.dto.RegisterDto;
import vesselems.model.User;
import vesselems.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("邮箱已被使用: " + dto.getEmail());
        }
        User user = new User();
        dto.apply(user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setModifyTime(LocalDateTime.now());
        user.setStatus(1);
        user = userRepository.save(user);

        return user;
    }
}