package emptyvessel.worklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emptyvessel.worklist.model.PermissionRole;

@Repository
public interface PermissionRoleRepository extends JpaRepository<PermissionRole, Long> {

    List<PermissionRole> findByRoleId(Long roleId);

    List<PermissionRole> findByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

}
