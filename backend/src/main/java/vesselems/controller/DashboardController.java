package vesselems.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.repository.ConfigRepository;
import vesselems.repository.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final ConfigRepository configRepository;

    public DashboardController(UserRepository userRepository, ConfigRepository configRepository) {
        this.userRepository = userRepository;
        this.configRepository = configRepository;
    }

    @GetMapping("/stats")
    public ApiResponse<java.util.Map<String, Object>> getStats(Authentication auth) {
        long userCount = userRepository.count();
        long configCount = configRepository.count();

        return ApiResponse.success(java.util.Map.of(
                "userCount", (int) userCount,
                "taskCount", (int) configCount));
    }
}