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
    public Role createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @PutMapping("/{id}")
    public Role updateRole(@PathVariable Long id, @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }

    @PostMapping("/{id}/menus")
    public void assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
    }

    @GetMapping("/{id}/menus")
    public List<RoleMenu> getRoleMenus(@PathVariable Long id) {
        return roleService.getRoleMenus(id);
    }

    @PostMapping("/{id}/permissions")
    public void assignPermissions(@PathVariable Long id, @RequestBody List<Long> permIds) {
        roleService.assignPermissions(id, permIds);
    }

    @GetMapping("/{id}/permissions")
    public List<PermissionRole> getRolePermissions(@PathVariable Long id) {
        return roleService.getRolePermissions(id);
    }

    /**
     * 获取按菜单分组的权限树，标记当前角色已分配的权限
     */
    @GetMapping("/{id}/permissions/tree")
    public List<Map<String, Object>> getPermissionTree(@PathVariable Long id) {
        // 获取当前角色已分配的权限ID集合
        List<PermissionRole> rolePerms = roleService.getRolePermissions(id);
        var assignedPermIds = rolePerms.stream()
                .map(PermissionRole::getPermissionId)
                .collect(Collectors.toSet());

        // 获取所有菜单（只取页面类型，即 menuType=1）
        List<Menu> allMenus = menuRepository.findAll().stream()
                .filter(m -> m.getMenuType() != null && m.getMenuType() == 1)
                .collect(Collectors.toList());

        // 构建菜单ID -> 菜单名映射
        Map<Long, String> menuNameMap = new HashMap<>();
        for (Menu m : allMenus) {
            menuNameMap.put(m.getId(), m.getMenuName());
        }

        // 按菜单分组权限
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