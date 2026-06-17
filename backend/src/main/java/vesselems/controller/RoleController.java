package vesselems.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.annotation.OperateLog;
import vesselems.model.Menu;
import vesselems.model.MenuPermission;
import vesselems.model.Permission;
import vesselems.model.PermissionRole;
import vesselems.model.Role;
import vesselems.model.RoleMenu;
import vesselems.repository.MenuPermissionRepository;
import vesselems.repository.MenuRepository;
import vesselems.repository.PermissionRepository;
import vesselems.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final MenuRepository menuRepository;
    private final MenuPermissionRepository menuPermissionRepository;
    private final PermissionRepository permissionRepository;

    public RoleController(RoleService roleService,
            MenuRepository menuRepository,
            MenuPermissionRepository menuPermissionRepository,
            PermissionRepository permissionRepository) {
        this.roleService = roleService;
        this.menuRepository = menuRepository;
        this.menuPermissionRepository = menuPermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<Role> listRoles() {
        return roleService.listRoles();
    }

    @GetMapping("/{id}")
    public Role getRole(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    @PostMapping
    @OperateLog(module = "角色管理", operation = "新增角色")
    public Role createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @PutMapping("/{id}")
    @OperateLog(module = "角色管理", operation = "修改角色")
    public Role updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    @OperateLog(module = "角色管理", operation = "删除角色")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    @PostMapping("/{id}/menus")
    @OperateLog(module = "角色管理", operation = "分配菜单")
    public void assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
    }

    @GetMapping("/{id}/menus")
    public List<RoleMenu> getRoleMenus(@PathVariable Long id) {
        return roleService.getRoleMenus(id);
    }

    @PostMapping("/{id}/permissions")
    @OperateLog(module = "角色管理", operation = "分配权限")
    public void assignPermissions(@PathVariable Long id, @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
    }

    @GetMapping("/{id}/permissions")
    public List<PermissionRole> getRolePermissions(@PathVariable Long id) {
        return roleService.getRolePermissions(id);
    }

    @GetMapping("/{id}/permissions/tree")
    public List<Map<String, Object>> getPermissionTree(@PathVariable Long id) {
        List<PermissionRole> rolePerms = roleService.getRolePermissions(id);
        var assignedPermIds = rolePerms.stream()
                .map(PermissionRole::getPermissionId)
                .collect(Collectors.toSet());

        List<Menu> allMenus = menuRepository.findAll().stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() == 1)
                .collect(Collectors.toList());

        Map<Long, String> menuNameMap = new HashMap<>();
        for (Menu m : allMenus) {
            menuNameMap.put(m.getId(), m.getMenuName());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Menu menu : allMenus) {
            List<MenuPermission> mpList = menuPermissionRepository.findByMenuId(menu.getId());
            if (mpList.isEmpty())
                continue;

            List<Map<String, Object>> permList = new ArrayList<>();
            for (MenuPermission mp : mpList) {
                Permission perm = permissionRepository.findById(mp.getPermissionId()).orElse(null);
                if (perm == null)
                    continue;
                Map<String, Object> permItem = new HashMap<>();
                permItem.put("id", perm.getId());
                permItem.put("permissionCode", perm.getPermissionCode());
                permItem.put("description", perm.getDescription());
                permItem.put("checked", assignedPermIds.contains(perm.getId()));
                permList.add(permItem);
            }

            if (!permList.isEmpty()) {
                Map<String, Object> group = new HashMap<>();
                group.put("menuId", menu.getId());
                group.put("menuName", menu.getMenuName());
                group.put("permissions", permList);
                result.add(group);
            }
        }

        return result;
    }
}