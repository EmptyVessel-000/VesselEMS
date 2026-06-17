package vesselems.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import vesselems.model.Menu;
import vesselems.repository.MenuRepository;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> listMenus() {
        return menuRepository.findAll();
    }

    public long countByType(int menuType) {
        return menuRepository.findAll().stream().filter(m -> m.getMenuType() == menuType).count();
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
        if (updated.getMenuName() != null)
            menu.setMenuName(updated.getMenuName());
        if (updated.getMenuPath() != null)
            menu.setMenuPath(updated.getMenuPath());
        if (updated.getMenuComponent() != null)
            menu.setMenuComponent(updated.getMenuComponent());
        if (updated.getMenuIcon() != null)
            menu.setMenuIcon(updated.getMenuIcon());
        if (updated.getMenuType() != null)
            menu.setMenuType(updated.getMenuType());
        if (updated.getParentId() != null)
            menu.setParentId(updated.getParentId());
        if (updated.getVisible() != null)
            menu.setVisible(updated.getVisible());
        if (updated.getSortOrder() != null)
            menu.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null)
            menu.setStatus(updated.getStatus());
        return menuRepository.save(menu);
    }

    public void deleteMenu(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new IllegalArgumentException("菜单不存在: " + id);
        }
        menuRepository.deleteById(id);
    }

    /**
     * 返回完整的菜单嵌套树（不过滤权限）
     */
    public List<Menu> getTree() {
        List<Menu> all = menuRepository.findAll();
        return buildTree(all);
    }

    private List<Menu> buildTree(List<Menu> all) {
        Map<Long, List<Menu>> childrenMap = all.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(Menu::getParentId));

        List<Menu> roots = new ArrayList<>();
        for (Menu menu : all) {
            List<Menu> children = childrenMap.get(menu.getId());
            if (children != null) {
                menu.setChildren(children);
                children.sort((a, b) -> {
                    int aOrder = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int bOrder = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return Integer.compare(aOrder, bOrder);
                });
            }
            if (menu.getParentId() == null) {
                roots.add(menu);
            }
        }
        roots.sort((a, b) -> {
            int aOrder = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int bOrder = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return Integer.compare(aOrder, bOrder);
        });
        return roots;
    }

    /**
     * 交换排序：找到同 parentId 下相邻兄弟，交换 sortOrder
     */
    public void moveMenu(Long id, String direction) {
        Menu current = getMenuById(id);
        Long parentId = current.getParentId();

        List<Menu> siblings = menuRepository.findByParentIdOrderBySortOrder(parentId);
        int idx = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx == -1)
            return;

        int targetIdx;
        if ("up".equalsIgnoreCase(direction)) {
            targetIdx = idx - 1;
        } else {
            targetIdx = idx + 1;
        }
        if (targetIdx < 0 || targetIdx >= siblings.size())
            return;

        Menu other = siblings.get(targetIdx);
        int tmp = current.getSortOrder() != null ? current.getSortOrder() : 0;
        current.setSortOrder(other.getSortOrder() != null ? other.getSortOrder() : 0);
        other.setSortOrder(tmp);

        menuRepository.save(current);
        menuRepository.save(other);
    }
}