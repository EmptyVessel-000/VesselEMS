package emptyvessel.worklist.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emptyvessel.worklist.common.ApiResponse;
import emptyvessel.worklist.dto.UserUpdateDto;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.success(userService.listUsers().orElse(null));
    }

    @GetMapping("/members")
    public ApiResponse<List<User>> listMembers() {
        return ApiResponse.success(userService.listMemberUsers());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id).orElse(null));
    }

    @DeleteMapping("/{id}")
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
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request) {
        userService.updateUser(id, request);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/password")
    public ApiResponse<Void> changePassword(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request) {
        userService.changePassword(id, request.password());
        return ApiResponse.success(null);
    }
}