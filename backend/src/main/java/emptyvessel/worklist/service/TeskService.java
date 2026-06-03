package emptyvessel.worklist.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import emptyvessel.worklist.dto.TeskWithNamesDto;
import emptyvessel.worklist.model.Tesk;
import emptyvessel.worklist.model.User;
import emptyvessel.worklist.repository.TeskRepository;
import emptyvessel.worklist.repository.UserRepository;

/**
 * 任务服务层
 * 处理任务的业务逻辑，包括创建、更新、删除、查询等操作
 * 协作模式：Guest（甲方）创建和审核，Member（乙方执行）执行，Manager（乙方协调）分配和管理
 */
@Service
public class TeskService {

    private final TeskRepository teskRepository;
    private final UserRepository userRepository;

    public TeskService(TeskRepository teskRepository, UserRepository userRepository) {
        this.teskRepository = teskRepository;
        this.userRepository = userRepository;
    }

    /**
     * 获取当前用户的所有任务（包含用户名）
     * - Member 用户：获取分配给自己的任务
     * - Guest 用户：获取自己创建的任务
     * - Manager 用户：获取所有任务
     * 
     * 使用 JOIN 查询一次性获取所有用户名，避免前端逐个查询
     */
    @PreAuthorize("isAuthenticated()")
    public List<TeskWithNamesDto> getUserTasks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        if (null != user.getRole())
            switch (user.getRole()) {
                case ROLE_MANAGER:
                    return teskRepository.findAllWithUserNames();
                case ROLE_MEMBER:
                    return teskRepository.findByAssignmentUserIdWithNames(user.getId());
                case ROLE_GUEST:
                    return teskRepository.findByCreatorUserIdWithNames(user.getId());
                default:
                    break;
            }

        return List.of();
    }

    /**
     * 获取单个任务
     */
    @PreAuthorize("isAuthenticated()")
    public Optional<Tesk> getTaskById(Long taskId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Optional<Tesk> tesk = teskRepository.findById(taskId);
        if (tesk.isEmpty()) {
            return tesk;
        }

        // 权限检查：只能查看自己的任务或管理员可以查看所有
        Tesk t = tesk.get();
        if (user.getRole() != User.Role.ROLE_MANAGER) {
            if (user.getRole() == User.Role.ROLE_MEMBER) {
                if (t.getAssignmentUserId() == null || !t.getAssignmentUserId().equals(user.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("无权查看该任务");
                }
            } else if (user.getRole() == User.Role.ROLE_GUEST) {
                if (!t.getCreatorUserId().equals(user.getId())) {
                    throw new org.springframework.security.access.AccessDeniedException("无权查看该任务");
                }
            }
        }

        return tesk;
    }

    /**
     * 创建任务
     * 只有 Guest 和 Manager 可以创建任务
     * 初始状态为 1（未分配）
     */
    @PreAuthorize("hasAnyRole('GUEST', 'MANAGER')")
    public Tesk createTask(Tesk tesk, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        tesk.setCreatorUserId(user.getId());
        tesk.setCreatedAt(LocalDateTime.now());
        tesk.setStatus(1); // 初始状态为未分配
        tesk.setRejectionCount(0);
        tesk.setIsDeleted(false);

        return teskRepository.save(tesk);
    }

    /**
     * 更新任务
     * 根据角色和状态限制可修改的字段
     * - Guest：只能修改 name、description、comment（任务要求、细节和沟通）
     * - Member：只能修改 status、comment（状态和沟通）
     * - Manager：可以修改任何字段
     */
    @PreAuthorize("isAuthenticated()")
    public Tesk updateTask(Long taskId, Tesk updateData, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (null != user.getRole()) // 权限检查
            switch (user.getRole()) {
                case ROLE_GUEST:
                    if (!tesk.getCreatorUserId().equals(user.getId())) {
                        throw new org.springframework.security.access.AccessDeniedException("无权更新该任务");
                    } // Guest 只能修改 name、description、comment
                    tesk.setName(updateData.getName());
                    tesk.setDescription(updateData.getDescription());
                    tesk.setComment(updateData.getComment());
                    break;
                case ROLE_MEMBER:
                    if (tesk.getAssignmentUserId() == null || !tesk.getAssignmentUserId().equals(user.getId())) {
                        throw new org.springframework.security.access.AccessDeniedException("无权更新该任务");
                    } // Member 只能修改 status、comment
                    tesk.setStatus(updateData.getStatus());
                    tesk.setComment(updateData.getComment());
                    break;
                case ROLE_MANAGER:
                    // Manager 可以修改任何字段
                    tesk.setName(updateData.getName());
                    tesk.setDescription(updateData.getDescription());
                    tesk.setStartTime(updateData.getStartTime());
                    tesk.setEndTime(updateData.getEndTime());
                    tesk.setStatus(updateData.getStatus());
                    tesk.setComment(updateData.getComment());
                    break;
                default:
                    break;
            }
        return teskRepository.save(tesk);
    }

    /**
     * 撤回任务（逻辑删除）
     * - Guest 用户：只能撤回状态为 1 的任务
     * - Manager 用户：可以撤回任何状态的任务
     */
    @PreAuthorize("hasAnyRole('GUEST', 'MANAGER')")
    public void withdrawTask(Long taskId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (user.getRole() == User.Role.ROLE_GUEST) {
            if (!tesk.getCreatorUserId().equals(user.getId())) {
                throw new org.springframework.security.access.AccessDeniedException("无权撤回该任务");
            }
            if (!tesk.getStatus().equals(1)) {
                throw new IllegalArgumentException("只能撤回未分配的任务");
            }
        }

        tesk.setIsDeleted(true);
        teskRepository.save(tesk);
    }

    /**
     * 分配任务给用户
     * 只有 Manager 可以分配任务
     * 状态从 1（未分配）变为 2（未开始），记录分配时间
     */
    @PreAuthorize("hasRole('MANAGER')")
    public Tesk assignTask(Long taskId, Long assigneeId) {
        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + assigneeId));

        if (!assignee.getRole().equals(User.Role.ROLE_MEMBER)) {
            throw new IllegalArgumentException("只能分配给 Member 用户");
        }

        tesk.assignToUser(assigneeId); // 使用模型方法，自动记录分配时间

        return teskRepository.save(tesk);
    }

    /**
     * 审核任务验收
     * 只有 Guest（创建者）可以审核
     * 状态从 4（验收中）变为 6（已完成）或 5（已打回）
     */
    @PreAuthorize("hasRole('GUEST')")
    public Tesk reviewTask(Long taskId, Integer decision, String comment, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (!tesk.getCreatorUserId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("无权审核该任务");
        }

        if (!tesk.getStatus().equals(4)) {
            throw new IllegalArgumentException("只能审核验收中的任务");
        }

        if (decision == 1) {
            // 通过：状态变为 6（已完成），记录完成时间
            tesk.markAsCompleted();
        } else if (decision == 0) {
            // 不通过：状态变为 5（已打回），打回次数加1
            tesk.rejectTask();
        } else {
            throw new IllegalArgumentException("决定值必须为 0 或 1");
        }

        tesk.setComment(comment);

        return teskRepository.save(tesk);
    }

    /**
     * Member 接受打回的任务，重新开始工作
     * 状态从 5（已打回）变为 2（未开始）
     */
    @PreAuthorize("hasRole('MEMBER')")
    public Tesk acceptRejectedTask(Long taskId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (tesk.getAssignmentUserId() == null || !tesk.getAssignmentUserId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("无权接受该任务");
        }

        if (!tesk.getStatus().equals(5)) {
            throw new IllegalArgumentException("只能接受已打回的任务");
        }

        tesk.setStatus(2); // 状态变为未开始

        return teskRepository.save(tesk);
    }

    /**
     * Member 提交任务进行验收
     * 状态从 3（进行中）变为 4（验收中），记录提交时间
     */
    @PreAuthorize("hasRole('MEMBER')")
    public Tesk submitForReview(Long taskId, String comment, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        Tesk tesk = teskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        if (tesk.getAssignmentUserId() == null || !tesk.getAssignmentUserId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("无权提交该任务");
        }

        if (!tesk.getStatus().equals(3)) {
            throw new IllegalArgumentException("只能提交进行中的任务");
        }

        tesk.submitForReview(comment); // 使用模型方法，自动记录提交时间

        return teskRepository.save(tesk);
    }

    /**
     * 获取 Member 的任务统计
     * 统计三个状态：未完成、验收中、已完成
     */
    @PreAuthorize("hasRole('MEMBER')")
    public Map<String, Object> getMemberStatistics(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));

        List<Tesk> memberTasks = teskRepository.findAll().stream()
                .filter(t -> !t.getIsDeleted() && t.getAssignmentUserId() != null
                        && t.getAssignmentUserId().equals(user.getId()))
                .toList();

        // 统计三个状态
        long incompleteCount = memberTasks.stream()
                .filter(t -> t.getStatus() == 1 || t.getStatus() == 2 || t.getStatus() == 3 || t.getStatus() == 5)
                .count();
        long reviewingCount = memberTasks.stream()
                .filter(t -> t.getStatus() == 4)
                .count();
        long completedCount = memberTasks.stream()
                .filter(t -> t.getStatus() == 6)
                .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("incomplete", incompleteCount);
        statistics.put("reviewing", reviewingCount);
        statistics.put("completed", completedCount);
        statistics.put("total", memberTasks.size());

        return statistics;
    }
}
