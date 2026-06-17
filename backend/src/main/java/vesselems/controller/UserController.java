package vesselems.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import vesselems.annotation.OperateLog;
import vesselems.common.ApiResponse;
import vesselems.dto.CreateUserDto;
import vesselems.dto.UserResponseDto;
import vesselems.dto.UserUpdateDto;
import vesselems.model.User;
import vesselems.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserResponseDto>> listUsers() {
        return ApiResponse.success(userService.listUsersWithRoles());
    }

    @GetMapping("/members")
    public ApiResponse<List<User>> listMembers() {
        return ApiResponse.success(userService.listMemberUsers());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id).orElse(null));
    }

    @PostMapping
    @OperateLog(module = "用户管理", operation = "新增用户")
    public ApiResponse<User> create(@Valid @RequestBody CreateUserDto request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @DeleteMapping("/{id}")
    @OperateLog(module = "用户管理", operation = "删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication auth) {
        if (!userService.isManager((Long) auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("无权操作");
        }
        if (userService.deleteUser(id)) {
            return ApiResponse.success(null);
        }
        throw new IllegalArgumentException("用户不存在");
    }

    @PutMapping("/{id}/info")
    @OperateLog(module = "用户管理", operation = "修改用户")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request) {
        userService.updateUser(id, request);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/password")
    public ApiResponse<Void> changePassword(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request) {
        userService.changePassword(id, request.getPassword());
        return ApiResponse.success(null);
    }

    @PostMapping("/import")
    @OperateLog(module = "用户管理", operation = "导入用户")
    public ApiResponse<Map<String, Object>> importUsers(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(userService.importUsers(file));
    }
}