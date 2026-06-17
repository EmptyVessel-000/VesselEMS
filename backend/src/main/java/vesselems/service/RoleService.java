package vesselems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vesselems.model.PermissionRole;
import vesselems.model.Role;
import vesselems.model.RoleMenu;
import vesselems.repository.PermissionRoleRepository;
import vesselems.repository.RoleMenuRepository;
import vesselems.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final PermissionRoleRepository permissionRoleRepository;

    public RoleService(RoleRepository roleRepository,
            RoleMenuRepository roleMenuRepository,
            PermissionRoleRepository permissionRoleRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.permissionRoleRepository = permissionRoleRepository;
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
        role.setCreateTime(LocalDateTime.now());
        role.setModifyTime(LocalDateTime.now());
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
        if (updated.getDescription() != null)
            role.setDescription(updated.getDescription());
        if (updated.getSortOrder() != null)
            role.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null)
            role.setStatus(updated.getStatus());
        role.setModifyTime(LocalDateTime.now());
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
        // 批量删除旧记录
        List<RoleMenu> oldMenus = roleMenuRepository.findByRoleId(roleId);
        if (!oldMenus.isEmpty()) {
            roleMenuRepository.deleteAll(oldMenus);
            roleMenuRepository.flush();
        }
        // 插入新记录
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
        // 批量删除旧记录
        List<PermissionRole> oldPerms = permissionRoleRepository.findByRoleId(roleId);
        if (!oldPerms.isEmpty()) {
            permissionRoleRepository.deleteAll(oldPerms);
            permissionRoleRepository.flush();
        }
        // 插入新记录
        for (Long permId : permIds) {
            PermissionRole rp = new PermissionRole();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            permissionRoleRepository.save(rp);
        }
    }

    public List<RoleMenu> getRoleMenus(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }

    public List<PermissionRole> getRolePermissions(Long roleId) {
        return permissionRoleRepository.findByRoleId(roleId);
    }
}