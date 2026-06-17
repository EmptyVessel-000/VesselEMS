package vesselems.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Log;
import vesselems.service.DatasourceService;
import vesselems.service.LogService;
import vesselems.service.MenuService;
import vesselems.service.ModelService;
import vesselems.service.UserService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserService userService;
    private final MenuService menuService;
    private final DatasourceService datasourceService;
    private final ModelService modelService;
    private final LogService logService;

    public DashboardController(UserService userService, MenuService menuService,
            DatasourceService datasourceService, ModelService modelService,
            LogService logService) {
        this.userService = userService;
        this.menuService = menuService;
        this.datasourceService = datasourceService;
        this.modelService = modelService;
        this.logService = logService;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.success(Map.of(
                "userCount", (int) userService.count(),
                "menuCount", (int) menuService.countByType(1),
                "dsCount", (int) datasourceService.count(),
                "modelCount", (int) modelService.count()));
    }

    @GetMapping("/recent-activities")
    public ApiResponse<List<Map<String, Object>>> getRecentActivities() {
        List<Log> logs = logService.getRecentActivities();
        List<Map<String, Object>> activities = logs.stream().map(log -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", log.getId());
            m.put("username", log.getUsername() != null ? log.getUsername() : "");
            m.put("module", log.getModule() != null ? log.getModule() : "");
            m.put("operation", log.getOperation() != null ? log.getOperation() : "");
            m.put("createTime", log.getCreateTime() != null ? log.getCreateTime().toString() : "");
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.success(activities);
    }
}
