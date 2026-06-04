package vesselems.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vesselems.model.RoleMenu;
import vesselems.repository.RoleMenuRepository;

@Service
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;

    public RoleMenuService(RoleMenuRepository roleMenuRepository) {
        this.roleMenuRepository = roleMenuRepository;
    }

    public List<RoleMenu> findByRoleId(Long roleId) {
        return roleMenuRepository.findByRoleId(roleId);
    }

    public List<RoleMenu> findByMenuId(Long menuId) {
        return roleMenuRepository.findByMenuId(menuId);
    }

    public void bind(Long roleId, Long menuId) {
        if (roleMenuRepository.existsByRoleIdAndMenuId(roleId, menuId)) {
            return;
        }
        RoleMenu rm = new RoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        roleMenuRepository.save(rm);
    }

    public void unbind(Long roleId, Long menuId) {
        roleMenuRepository.deleteByRoleIdAndMenuId(roleId, menuId);
    }
}