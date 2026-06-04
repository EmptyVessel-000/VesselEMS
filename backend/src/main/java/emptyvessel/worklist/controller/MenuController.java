package emptyvessel.worklist.controller;

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

import emptyvessel.worklist.common.ApiResponse;
import emptyvessel.worklist.model.Menu;
import emptyvessel.worklist.service.MenuService;

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
    public ApiResponse<Menu> create(@RequestBody Menu menu) {
        return ApiResponse.success(menuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    public ApiResponse<Menu> update(@PathVariable Long id, @RequestBody Menu menu) {
        return ApiResponse.success(menuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.success(null);
    }
}