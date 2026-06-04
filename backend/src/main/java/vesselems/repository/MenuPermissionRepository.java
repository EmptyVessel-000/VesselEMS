package vesselems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.MenuPermission;

@Repository
public interface MenuPermissionRepository extends JpaRepository<MenuPermission, Long> {

    List<MenuPermission> findByMenuId(Long menuId);

    List<MenuPermission> findByPermissionId(Long permissionId);

    boolean existsByMenuIdAndPermissionId(Long menuId, Long permissionId);

    void deleteByMenuIdAndPermissionId(Long menuId, Long permissionId);
}