package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.RolePermission;
import emptyvessel.worklist.repository.RolePermissionRepository;

@Service
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public List<RolePermission> findByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    public List<RolePermission> findByPermissionId(Long permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    public void bind(Long roleId, Long permissionId) {
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return;
        }
        RolePermission rp = new RolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        rolePermissionRepository.save(rp);
    }

    public void unbind(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }
}