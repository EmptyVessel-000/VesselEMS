package vesselems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Config;
import vesselems.service.ConfigService;

@RestController
@RequestMapping("/api/configs")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ApiResponse<List<Config>> list() {
        return ApiResponse.success(configService.listConfigs());
    }

    @GetMapping("/{id}")
    public ApiResponse<Config> getById(@PathVariable Long id) {
        return ApiResponse.success(configService.getConfigById(id));
    }

    @PostMapping
    public ApiResponse<Config> create(@RequestBody Config config) {
        return ApiResponse.success(configService.createConfig(config));
    }

    @PutMapping("/{id}")
    public ApiResponse<Config> update(@PathVariable Long id, @RequestBody Config config) {
        return ApiResponse.success(configService.updateConfig(id, config));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ApiResponse.success(null);
    }
}