package vesselems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.RoleMenu;

@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenu, Long> {

    List<RoleMenu> findByRoleId(Long roleId);

    List<RoleMenu> findByMenuId(Long menuId);

    boolean existsByRoleIdAndMenuId(Long roleId, Long menuId);

    void deleteByRoleIdAndMenuId(Long roleId, Long menuId);
}