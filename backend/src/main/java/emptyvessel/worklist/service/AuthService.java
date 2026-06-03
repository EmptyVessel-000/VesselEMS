package emptyvessel.worklist.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.dto.RegisterDto;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("邮箱已被使用: " + dto.email());
        }
        User user = new User();
        dto.apply(user);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        return userRepository.save(user);
    }
}