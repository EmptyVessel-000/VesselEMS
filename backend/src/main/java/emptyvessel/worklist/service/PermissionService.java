package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.Permission;
import emptyvessel.worklist.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
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
        if (updated.getPermissionCode() != null && !updated.getPermissionCode().equals(permission.getPermissionCode())) {
            if (permissionRepository.existsByPermissionCode(updated.getPermissionCode())) {
                throw new IllegalArgumentException("权限标识已存在: " + updated.getPermissionCode());
            }
            permission.setPermissionCode(updated.getPermissionCode());
        }
        if (updated.getDescription() != null) permission.setDescription(updated.getDescription());
        if (updated.getSortOrder() != null) permission.setSortOrder(updated.getSortOrder());
        return permissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new IllegalArgumentException("权限不存在: " + id);
        }
        permissionRepository.deleteById(id);
    }
}