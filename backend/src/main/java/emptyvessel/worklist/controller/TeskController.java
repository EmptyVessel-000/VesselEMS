package emptyvessel.worklist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import emptyvessel.worklist.dto.TeskWithNamesDto;
import emptyvessel.worklist.model.Tesk;
import emptyvessel.worklist.service.TeskService;

/**
 * 任务控制层
 * 处理任务相关的 HTTP 请求
 * 权限控制由 TeskService 中的 @PreAuthorize 注解处理
 */
@RestController
@RequestMapping("/api/tesk")
public class TeskController {

    private final TeskService teskService;

    public TeskController(TeskService teskService) {
        this.teskService = teskService;
    }

    /**
     * 获取当前用户的所有任务（包含用户名）
     */
    @GetMapping
    public ResponseEntity<List<TeskWithNamesDto>> getAll(java.security.Principal principal) {
        try {
            List<TeskWithNamesDto> tasks = teskService.getUserTasks(principal.getName());
            return ResponseEntity.ok(tasks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 获取单个任务
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tesk> getById(@PathVariable Long id, java.security.Principal principal) {
        try {
            Tesk tesk = teskService.getTaskById(id, principal.getName()).orElseThrow();
            return ResponseEntity.ok(tesk);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Tesk tesk, java.security.Principal principal) {
        try {
            if (tesk.getName() == null || tesk.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("任务名称不能为空");
            }
            if (tesk.getName().length() > 30) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("任务名称长度不能超过 30");
            }
            if (tesk.getDescription() != null && tesk.getDescription().length() > 500) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("任务描述长度不能超过 500");
            }

            Tesk saved = teskService.createTask(tesk, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Tesk tesk,
            java.security.Principal principal) {
        try {
            if (tesk.getName() != null && tesk.getName().length() > 30) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("任务名称长度不能超过 30");
            }
            if (tesk.getDescription() != null && tesk.getDescription().length() > 500) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("任务描述长度不能超过 500");
            }

            Tesk updated = teskService.updateTask(id, tesk, principal.getName());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 撤回任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> withdraw(@PathVariable Long id, java.security.Principal principal) {
        try {
            teskService.withdrawTask(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 分配任务给用户
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<Object> assign(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        try {
            Long assigneeId = body.get("assigneeId");
            if (assigneeId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("assigneeId 不能为空");
            }

            Tesk assigned = teskService.assignTask(id, assigneeId);
            return ResponseEntity.ok(assigned);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 审核任务验收
     * decision: 1 = 通过，0 = 打回
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<Object> review(@PathVariable Long id, @RequestBody Map<String, Object> body,
            java.security.Principal principal) {
        try {
            Integer decision = ((Number) body.get("decision")).intValue();
            String comment = (String) body.getOrDefault("comment", "");

            Tesk reviewed = teskService.reviewTask(id, decision, comment, principal.getName());
            return ResponseEntity.ok(reviewed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Member 接受打回的任务
     */
    @PostMapping("/{id}/accept-reject")
    public ResponseEntity<Object> acceptReject(@PathVariable Long id, java.security.Principal principal) {
        try {
            Tesk accepted = teskService.acceptRejectedTask(id, principal.getName());
            return ResponseEntity.ok(accepted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * Member 提交任务进行验收
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Object> submit(@PathVariable Long id, @RequestBody Map<String, String> body,
            java.security.Principal principal) {
        try {
            String comment = body.getOrDefault("comment", "");
            Tesk submitted = teskService.submitForReview(id, comment, principal.getName());
            return ResponseEntity.ok(submitted);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /**
     * 获取 Member 的任务统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(java.security.Principal principal) {
        try {
            Map<String, Object> statistics = teskService.getMemberStatistics(principal.getName());
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
