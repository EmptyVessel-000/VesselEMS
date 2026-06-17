package vesselems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vesselems.annotation.OperateLog;
import vesselems.common.ApiResponse;
import vesselems.model.Menu;
import vesselems.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<Menu>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean enabled) {
        if (parentId != null) {
            return ApiResponse.success(menuService.listByParentId(parentId));
        }
        if (Boolean.TRUE.equals(enabled)) {
            return ApiResponse.success(menuService.listEnabled());
        }
        return ApiResponse.success(menuService.listMenus());
    }

    @GetMapping("/{id}")
    public ApiResponse<Menu> getById(@PathVariable Long id) {
        return ApiResponse.success(menuService.getMenuById(id));
    }

    @PostMapping
    @OperateLog(module = "菜单管理", operation = "新增菜单")
    public ApiResponse<Menu> create(@RequestBody Menu menu) {
        return ApiResponse.success(menuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    @OperateLog(module = "菜单管理", operation = "修改菜单")
    public ApiResponse<Menu> update(@PathVariable Long id, @RequestBody Menu menu) {
        return ApiResponse.success(menuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    @OperateLog(module = "菜单管理", operation = "删除菜单")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/tree")
    public ApiResponse<List<Menu>> tree() {
        return ApiResponse.success(menuService.getTree());
    }

    @PostMapping("/{id}/move")
    @OperateLog(module = "菜单管理", operation = "移动菜单排序")
    public ApiResponse<Void> move(@PathVariable Long id, @RequestParam String direction) {
        menuService.moveMenu(id, direction);
        return ApiResponse.success(null);
    }
}