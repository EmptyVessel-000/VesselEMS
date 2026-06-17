package vesselems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.annotation.OperateLog;
import vesselems.common.ApiResponse;
import vesselems.model.Permission;
import vesselems.service.PermissionService;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ApiResponse<List<Permission>> list() {
        return ApiResponse.success(permissionService.listPermissions());
    }

    @GetMapping("/{id}")
    public ApiResponse<Permission> getById(@PathVariable Long id) {
        return ApiResponse.success(permissionService.getPermissionById(id));
    }

    @PostMapping
    @OperateLog(module = "权限管理", operation = "新增权限")
    public ApiResponse<Permission> create(@RequestBody Permission permission) {
        return ApiResponse.success(permissionService.createPermission(permission));
    }

    @PutMapping("/{id}")
    @OperateLog(module = "权限管理", operation = "修改权限")
    public ApiResponse<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        return ApiResponse.success(permissionService.updatePermission(id, permission));
    }

    @DeleteMapping("/{id}")
    @OperateLog(module = "权限管理", operation = "删除权限")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null);
    }
}