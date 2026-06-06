package vesselems.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vesselems.dto.CreateUserDto;
import vesselems.dto.UserResponseDto;
import vesselems.dto.UserUpdateDto;
import vesselems.model.User;
import vesselems.model.UserRole;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRepository;
import vesselems.repository.UserRoleRepository;

@Service
public class UserService {

    private static final String SUPER_ADMIN_ROLE_NAME = "super_admin";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDto> listUsersWithRoles() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> result = new ArrayList<>();
        for (User u : users) {
            result.add(buildUserResponse(u));
        }
        return result;
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

    public User createUser(CreateUserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setGender(dto.getGender());
        user.setTelephone(dto.getTelephone());
        user.setDepartmentId(dto.getDepartmentId());
        user.setRemark(dto.getRemark());
        user.setStatus(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
        user.setCreateTime(LocalDateTime.now());
        user.setModifyTime(LocalDateTime.now());
        user = userRepository.save(user);

        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            syncUserRoles(user.getId(), dto.getRoles());
        }

        return user;
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id))
            return false;
        if (isSuperAdmin(id)) {
            throw new IllegalArgumentException("超级管理员不可删除");
        }
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

        if (isSuperAdmin(id)) {
            throw new IllegalArgumentException("超级管理员信息不可修改");
        }

        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());
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
        if (dto.getRemark() != null)
            user.setRemark(dto.getRemark());
        if (dto.getEnabled() != null)
            user.setStatus(dto.getEnabled() ? 1 : 0);
        user.setModifyTime(LocalDateTime.now());
        userRepository.save(user);

        if (dto.getRoles() != null) {
            syncUserRoles(id, dto.getRoles());
        }
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

    /** Whether the given user has the SUPER_ADMIN role */
    public boolean isSuperAdmin(Long userId) {
        Long superAdminRoleId = findSuperAdminRoleId();
        if (superAdminRoleId == null)
            return false;
        return userRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> ur.getRoleId().equals(superAdminRoleId));
    }

    /**
     * Sync a user's role assignments. Filters out the super_admin role
     * so it can never be assigned via the API.
     */
    public void syncUserRoles(Long userId, List<Long> roleIds) {
        Long superAdminRoleId = findSuperAdminRoleId();

        // Filter out super_admin role id
        List<Long> filtered = roleIds.stream()
                .filter(rid -> superAdminRoleId == null || !rid.equals(superAdminRoleId))
                .distinct()
                .collect(Collectors.toList());

        // Remove all existing role assignments for this user
        List<UserRole> existing = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(existing);

        // Insert new role assignments
        for (Long roleId : filtered) {
            if (roleRepository.existsById(roleId)) {
                UserRole ur = new UserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            }
        }
    }

    private UserResponseDto buildUserResponse(User u) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setNickname(u.getNickname());
        dto.setEmail(u.getEmail());
        dto.setTelephone(u.getTelephone());
        dto.setStatus(u.getStatus());
        dto.setCreateTime(u.getCreateTime());

        List<UserRole> userRoles = userRoleRepository.findByUserId(u.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        dto.setRoleIds(roleIds);

        List<String> roleNames = userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                .filter(r -> r != null)
                .map(r -> r.getRoleName())
                .collect(Collectors.toList());
        dto.setRoleNames(roleNames);

        boolean isSuper = roleNames.contains(SUPER_ADMIN_ROLE_NAME);
        dto.setIsSuperAdmin(isSuper);

        return dto;
    }

    private Long findSuperAdminRoleId() {
        return roleRepository.findByRoleName(SUPER_ADMIN_ROLE_NAME)
                .map(r -> r.getId())
                .orElse(null);
    }

    private Long findMemberRoleId() {
        return 2L; // ROLE_MEMBER = 2
    }

    private Long findManagerRoleId() {
        return 3L; // ROLE_MANAGER = 3
    }
}