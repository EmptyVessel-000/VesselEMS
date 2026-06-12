package vesselems.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void updateLoginInfo(User user) {
        userRepository.save(user);
    }

    public long count() {
        return userRepository.count();
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

    /**
     * Import users from CSV or Excel file.
     * Format: username,password,email,realName,telephone
     * Gender defaults to 0 (unknown).
     */
    public Map<String, Object> importUsers(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String lower = filename.toLowerCase();

        List<String[]> rows;
        if (lower.endsWith(".csv")) {
            rows = parseCsv(file);
        } else if (lower.endsWith(".xlsx")) {
            rows = parseExcel(file, true);
        } else if (lower.endsWith(".xls")) {
            rows = parseExcel(file, false);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，请上传 .csv/.xlsx/.xls 文件");
        }

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            String[] cols = rows.get(i);
            // Skip empty rows
            if (cols.length == 0 || (cols.length == 1 && cols[0].isBlank()))
                continue;
            try {
                if (cols.length < 3) {
                    throw new IllegalArgumentException("列数不足，至少需要用户名、密码、邮箱三列");
                }
                String username = cols[0].trim();
                String password = cols.length > 1 ? cols[1].trim() : "";
                String email = cols.length > 2 ? cols[2].trim() : "";
                String realName = cols.length > 3 ? cols[3].trim() : "";
                String telephone = cols.length > 4 ? cols[4].trim() : "";

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    throw new IllegalArgumentException("用户名、密码、邮箱不能为空");
                }
                if (userRepository.existsByUsername(username)) {
                    throw new IllegalArgumentException("用户名已存在: " + username);
                }
                if (userRepository.existsByEmail(email)) {
                    throw new IllegalArgumentException("邮箱已被使用: " + email);
                }

                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setEmail(email);
                user.setRealName(realName);
                user.setTelephone(telephone);
                user.setGender(0); // default unknown
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setModifyTime(LocalDateTime.now());
                userRepository.save(user);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add("第" + (i + 1) + "行: " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", rows.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);
        return result;
    }

    private List<String[]> parseCsv(MultipartFile file) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Strip UTF-8 BOM if present
                if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                if (line.isEmpty())
                    continue;
                // Always skip the first line (header row)
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                rows.add(line.split(",", -1));
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV读取失败: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<String[]> parseExcel(MultipartFile file, boolean isXlsx) {
        List<String[]> rows = new ArrayList<>();
        try (Workbook wb = isXlsx ? new XSSFWorkbook(file.getInputStream()) : new HSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue;
                }
                int cols = Math.max(row.getLastCellNum(), 5);
                String[] cells = new String[cols];
                boolean hasData = false;
                for (int c = 0; c < cols; c++) {
                    var cell = row.getCell(c);
                    if (cell != null) {
                        cells[c] = cell.toString().trim();
                        if (!cells[c].isEmpty())
                            hasData = true;
                    } else {
                        cells[c] = "";
                    }
                }
                if (hasData)
                    rows.add(cells);
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel读取失败: " + e.getMessage(), e);
        }
        return rows;
    }
}