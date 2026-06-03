package emptyvessel.worklist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.dto.UserUpdateDto;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 核心身份权限校验逻辑（下沉至 Service 层）
    public void verifyOwnership(Long targetUserId, String currentPrincipalEmail) {
        User currentUser = userRepository.findByEmail(currentPrincipalEmail)
                .orElseThrow(() -> new IllegalArgumentException("当前用户未登录"));

        // 管理员级别角色可以访问所有，普通成员只能访问自己的 ID
        if (currentUser.getRole() != User.Role.ROLE_MANAGER && !currentUser.getId().equals(targetUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("无权访问该资源");
        }
    }

    public Optional<List<User>> listUsers() {
        return Optional.of(userRepository.findAll());
    }

    public List<User> listMemberUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.ROLE_MEMBER)
                .toList();
    }

    public User createUser(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id))
            return false;
        userRepository.deleteById(id);
        return true;
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void updateUser(Long userId, UserUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        dto.apply(user);
        userRepository.save(user);
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}