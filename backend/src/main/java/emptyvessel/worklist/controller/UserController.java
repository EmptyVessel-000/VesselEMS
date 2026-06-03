package emptyvessel.worklist.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 列出所有用户，仅管理员可见
    @GetMapping
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.of(userService.listUsers());
    }

    // 获取所有 Member 角色的用户列表（用于分配任务）
    @GetMapping("/members")
    public ResponseEntity<List<User>> listMembers() {
        return ResponseEntity.ok(userService.listMemberUsers());
    }

    // 获取用户详情
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id, java.security.Principal principal) {
        userService.verifyOwnership(id, principal.getName());
        return ResponseEntity.of(userService.getUserById(id));
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, java.security.Principal principal) {
        // 删除操作仅限 Manager，此处通过简单的 Manager 角色校验
        var user = userService.getUserByEmail(principal.getName()).orElseThrow();
        if (user.getRole() != emptyvessel.worklist.model.User.Role.ROLE_MANAGER) {
            throw new org.springframework.security.access.AccessDeniedException("无权操作");
        }
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 更新用户信息
    @PutMapping("/{id}/info")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request, java.security.Principal principal) {
        userService.verifyOwnership(id, principal.getName());
        userService.updateUser(id, request);
        return ResponseEntity.noContent().build();
    }

    // 修改密码
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id, @Valid @RequestBody UserUpdateDto request, java.security.Principal principal) {
        userService.verifyOwnership(id, principal.getName());
        userService.changePassword(id, request.password());
        return ResponseEntity.noContent().build();
    }
}
