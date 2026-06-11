package vesselems.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.service.ConfigService;
import vesselems.service.UserService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserService userService;
    private final ConfigService configService;

    public DashboardController(UserService userService, ConfigService configService) {
        this.userService = userService;
        this.configService = configService;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        long userCount = userService.count();
        long configCount = configService.count();

        return ApiResponse.success(Map.of(
                "userCount", (int) userCount,
                "taskCount", (int) configCount));
    }
}