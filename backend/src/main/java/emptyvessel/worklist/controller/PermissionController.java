package emptyvessel.worklist.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emptyvessel.worklist.common.ApiResponse;
import emptyvessel.worklist.model.Permission;
import emptyvessel.worklist.service.PermissionService;

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
    public ApiResponse<Permission> create(@RequestBody Permission permission) {
        return ApiResponse.success(permissionService.createPermission(permission));
    }

    @PutMapping("/{id}")
    public ApiResponse<Permission> update(@PathVariable Long id, @RequestBody Permission permission) {
        return ApiResponse.success(permissionService.updatePermission(id, permission));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ApiResponse.success(null);
    }
}