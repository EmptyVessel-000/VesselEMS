package vesselems.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import vesselems.model.Permission;
import vesselems.model.Role;
import vesselems.repository.MenuPermissionRepository;
import vesselems.repository.PermissionRepository;
import vesselems.repository.RoleRepository;
import vesselems.repository.UserRoleRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuService roleMenuService;
    private final PermissionRoleService permissionRoleService;
    private final MenuPermissionRepository menuPermissionRepository;
    private final MenuService menuService;

    public PermissionService(PermissionRepository permissionRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            RoleMenuService roleMenuService,
            PermissionRoleService permissionRoleService,
            MenuPermissionRepository menuPermissionRepository,
            MenuService menuService) {
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.roleMenuService = roleMenuService;
        this.permissionRoleService = permissionRoleService;
        this.menuPermissionRepository = menuPermissionRepository;
        this.menuService = menuService;
    }

    public List<Permission> listPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("权限不存在: " + id));
    }

    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByPermissionCode(permission.getPermissionCode())) {
            throw new IllegalArgumentException("权限标识已存在: " + permission.getPermissionCode());
        }
        return permissionRepository.save(permission);
    }

    public Permission updatePermission(Long id, Permission updated) {
        Permission permission = getPermissionById(id);
        if (updated.getPermissionCode() != null
                && !updated.getPermissionCode().equals(permission.getPermissionCode())) {
            if (permissionRepository.existsByPermissionCode(updated.getPermissionCode())) {
                throw new IllegalArgumentException("权限标识已存在: " + updated.getPermissionCode());
            }
            permission.setPermissionCode(updated.getPermissionCode());
        }
        if (updated.getDescription() != null)
            permission.setDescription(updated.getDescription());
        if (updated.getSortOrder() != null)
            permission.setSortOrder(updated.getSortOrder());
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new IllegalArgumentException("权限不存在: " + id);
        }
        permissionRepository.deleteById(id);
    }

    /**
     * Calculate the complete permission set for a given user.
     * Returns { menus: Set<Long>, permissions: Set<String>, menuTree: List<Menu> }
     */
    public Map<String, Object> calculateUserPermissions(Long userId) {
        boolean isSuperAdmin = userRoleRepository.findByUserId(userId).stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()).orElse(null))
                .filter(r -> r != null)
                .anyMatch(r -> "super_admin".equals(r.getRoleName()));

        if (isSuperAdmin) {
            return Map.of(
                    "menus", Set.of(-1L),
                    "permissions", Set.of("*"),
                    "menuTree", menuService.getTree());
        }

        Set<Long> menuIds = new LinkedHashSet<>();
        Set<Long> permissionIds = new LinkedHashSet<>();

        for (var ur : userRoleRepository.findByUserId(userId)) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role == null || (role.getStatus() != null && role.getStatus() != 1))
                continue;
            roleMenuService.findByRoleId(ur.getRoleId())
                    .forEach(rm -> menuIds.add(rm.getMenuId()));
            permissionRoleService.findByRoleId(ur.getRoleId())
                    .forEach(rp -> permissionIds.add(rp.getPermissionId()));
        }

        Map<Long, Set<Long>> permMenuMap = new LinkedHashMap<>();
        for (var mp : menuPermissionRepository.findAll()) {
            permMenuMap.computeIfAbsent(mp.getPermissionId(), k -> new LinkedHashSet<>())
                    .add(mp.getMenuId());
        }

        Set<String> permCodes = new LinkedHashSet<>();
        for (Long permId : permissionIds) {
            try {
                String code = getPermissionById(permId).getPermissionCode();
                Set<Long> requiredMenus = permMenuMap.get(permId);
                if (requiredMenus == null || requiredMenus.isEmpty()) {
                    permCodes.add(code);
                } else {
                    requiredMenus.retainAll(menuIds);
                    if (!requiredMenus.isEmpty()) {
                        permCodes.add(code);
                    }
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Map.of(
                "menus", menuIds,
                "permissions", permCodes,
                "menuTree", menuService.getTree());
    }
}