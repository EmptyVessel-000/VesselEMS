package emptyvessel.worklist.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import emptyvessel.worklist.common.ApiResponse;
import emptyvessel.worklist.model.Log;
import emptyvessel.worklist.service.LogService;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public ApiResponse<List<Log>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        if (userId != null) {
            return ApiResponse.success(logService.findByUserId(userId));
        }
        if (module != null) {
            return ApiResponse.success(logService.findByModule(module));
        }
        if (operation != null) {
            return ApiResponse.success(logService.findByOperation(operation));
        }
        if (start != null && end != null) {
            return ApiResponse.success(logService.findByTimeRange(start, end));
        }
        return ApiResponse.success(logService.listLogs());
    }

    @GetMapping("/{id}")
    public ApiResponse<Log> getById(@PathVariable Long id) {
        return ApiResponse.success(logService.getLogById(id));
    }

    @PostMapping
    public ApiResponse<Log> record(@RequestBody Log log) {
        return ApiResponse.success(logService.record(log));
    }

    @DeleteMapping("/clean")
    public ApiResponse<Void> clean(@RequestParam(defaultValue = "90") int days) {
        logService.cleanOldLogs(days);
        return ApiResponse.success(null);
    }
}