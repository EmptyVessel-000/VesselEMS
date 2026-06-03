package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.MenuPermission;
import emptyvessel.worklist.repository.MenuPermissionRepository;

@Service
public class MenuPermissionService {

    private final MenuPermissionRepository menuPermissionRepository;

    public MenuPermissionService(MenuPermissionRepository menuPermissionRepository) {
        this.menuPermissionRepository = menuPermissionRepository;
    }

    public List<MenuPermission> findByMenuId(Long menuId) {
        return menuPermissionRepository.findByMenuId(menuId);
    }

    public List<MenuPermission> findByPermissionId(Long permissionId) {
        return menuPermissionRepository.findByPermissionId(permissionId);
    }

    public void bind(Long menuId, Long permissionId) {
        if (menuPermissionRepository.existsByMenuIdAndPermissionId(menuId, permissionId)) {
            return;
        }
        MenuPermission mp = new MenuPermission();
        mp.setMenuId(menuId);
        mp.setPermissionId(permissionId);
        menuPermissionRepository.save(mp);
    }

    public void unbind(Long menuId, Long permissionId) {
        menuPermissionRepository.deleteByMenuIdAndPermissionId(menuId, permissionId);
    }
}