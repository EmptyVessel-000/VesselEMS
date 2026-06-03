package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import emptyvessel.worklist.model.Role;
import emptyvessel.worklist.model.RoleMenu;
import emptyvessel.worklist.model.RolePermission;
import emptyvessel.worklist.repository.RoleMenuRepository;
import emptyvessel.worklist.repository.RolePermissionRepository;
import emptyvessel.worklist.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleService(RoleRepository roleRepository,
                       RoleMenuRepository roleMenuRepository,
                       RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public List<Role> listRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
    }

    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new IllegalArgumentException("角色标识已存在: " + role.getRoleName());
        }
        role.setStatus(role.getStatus() != null ? role.getStatus() : 1);
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, Role updated) {
        Role role = getRoleById(id);
        if (updated.getRoleName() != null && !updated.getRoleName().equals(role.getRoleName())) {
            if (roleRepository.existsByRoleName(updated.getRoleName())) {
                throw new IllegalArgumentException("角色标识已存在: " + updated.getRoleName());
            }
            role.setRoleName(updated.getRoleName());
        }
        if (updated.getDescription() != null) role.setDescription(updated.getDescription());
        if (updated.getSortOrder() != null) role.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null) role.setStatus(updated.getStatus());
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("角色不存在: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        getRoleById(roleId);
        roleMenuRepository.findByRoleId(roleId).forEach(rm -> roleMenuRepository.deleteById(rm.getId()));
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuRepository.save(rm);
        }
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permIds) {
        getRoleById(roleId);
        rolePermissionRepository.findByRoleId(roleId).forEach(rp -> rolePermissionRepository.deleteById(rp.getId()));
        for (Long permId : permIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionRepository.save(rp);
        }
    }

    public List<RoleMenu> getRoleMenus(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }
}