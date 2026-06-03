package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.Menu;
import emptyvessel.worklist.repository.MenuRepository;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> listMenus() {
        return menuRepository.findAll();
    }

    public List<Menu> listByParentId(Long parentId) {
        return menuRepository.findByParentIdOrderBySortOrder(parentId);
    }

    public List<Menu> listEnabled() {
        return menuRepository.findByStatusOrderBySortOrder(1);
    }

    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));
    }

    public Menu createMenu(Menu menu) {
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        return menuRepository.save(menu);
    }

    public Menu updateMenu(Long id, Menu updated) {
        Menu menu = getMenuById(id);
        if (updated.getMenuName() != null) menu.setMenuName(updated.getMenuName());
        if (updated.getMenuPath() != null) menu.setMenuPath(updated.getMenuPath());
        if (updated.getMenuComponent() != null) menu.setMenuComponent(updated.getMenuComponent());
        if (updated.getMenuIcon() != null) menu.setMenuIcon(updated.getMenuIcon());
        if (updated.getMenuType() != null) menu.setMenuType(updated.getMenuType());
        if (updated.getParentId() != null) menu.setParentId(updated.getParentId());
        if (updated.getVisible() != null) menu.setVisible(updated.getVisible());
        if (updated.getIsFrame() != null) menu.setIsFrame(updated.getIsFrame());
        if (updated.getPermission() != null) menu.setPermission(updated.getPermission());
        if (updated.getSortOrder() != null) menu.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null) menu.setStatus(updated.getStatus());
        return menuRepository.save(menu);
    }

    public void deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new IllegalArgumentException("菜单不存在: " + id);
        }
        menuRepository.deleteById(id);
    }
}