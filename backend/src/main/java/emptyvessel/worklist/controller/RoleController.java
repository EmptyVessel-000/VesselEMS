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
import emptyvessel.worklist.model.PermissionRole;
import emptyvessel.worklist.model.Role;
import emptyvessel.worklist.model.RoleMenu;
import emptyvessel.worklist.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<Role>> list() {
        return ApiResponse.success(roleService.listRoles());
    }

    @GetMapping("/{id}")
    public ApiResponse<Role> getById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @PostMapping
    public ApiResponse<Role> create(@RequestBody Role role) {
        return ApiResponse.success(roleService.createRole(role));
    }

    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable Long id, @RequestBody Role role) {
        return ApiResponse.success(roleService.updateRole(id, role));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/menus")
    public ApiResponse<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/permissions")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/menus")
    public ApiResponse<List<RoleMenu>> getMenus(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleMenus(id));
    }

    @GetMapping("/{id}/permissions")
    public ApiResponse<List<PermissionRole>> getPermissions(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRolePermissions(id));
    }
}