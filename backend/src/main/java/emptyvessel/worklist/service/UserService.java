package emptyvessel.worklist.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.dto.UserUpdateDto;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.repository.UserRepository;
import emptyvessel.worklist.repository.UserRoleRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<List<User>> listUsers() {
        return Optional.of(userRepository.findAll());
    }

    public List<User> listMemberUsers() {
        return userRoleRepository.findByRoleId(findMemberRoleId()).stream()
                .map(ur -> userRepository.findById(ur.getUserId()).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    public void updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
        if (dto.getNickname() != null)
            user.setNickname(dto.getNickname());
        if (dto.getRealName() != null)
            user.setRealName(dto.getRealName());
        if (dto.getGender() != null)
            user.setGender(dto.getGender());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getTelephone() != null)
            user.setTelephone(dto.getTelephone());
        if (dto.getDepartmentId() != null)
            user.setDepartmentId(dto.getDepartmentId());
        userRepository.save(user);
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** Whether the given user has the MANAGER role */
    public boolean isManager(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> ur.getRoleId().equals(findManagerRoleId()));
    }

    private Long findMemberRoleId() {
        return 2L; // ROLE_MEMBER = 2
    }

    private Long findManagerRoleId() {
        return 3L; // ROLE_MANAGER = 3
    }
}