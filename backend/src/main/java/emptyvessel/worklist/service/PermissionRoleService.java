package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.PermissionRole;
import emptyvessel.worklist.repository.PermissionRoleRepository;

@Service
public class PermissionRoleService {

    private final PermissionRoleRepository PermissionRoleRepository;

    public PermissionRoleService(PermissionRoleRepository PermissionRoleRepository) {
        this.PermissionRoleRepository = PermissionRoleRepository;
    }

    public List<PermissionRole> findByRoleId(Long roleId) {
        return PermissionRoleRepository.findByRoleId(roleId);
    }

    public List<PermissionRole> findByPermissionId(Long permissionId) {
        return PermissionRoleRepository.findByPermissionId(permissionId);
    }

    public void bind(Long roleId, Long permissionId) {
        if (PermissionRoleRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            return;
        }
        PermissionRole rp = new PermissionRole();
        rp.setRoleId(roleId);
        rp.setPermissionId(permissionId);
        PermissionRoleRepository.save(rp);
    }

    public void unbind(Long roleId, Long permissionId) {
        PermissionRoleRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }
}