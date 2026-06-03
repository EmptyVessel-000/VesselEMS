package emptyvessel.worklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Tesk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name; // 任务名称

    @Column(name = "description", length = 500)
    private String description; // 任务介绍

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId; // 创建者用户ID

    @Column(name = "assignment_user_id")
    private Long assignmentUserId; // 分配给的用户ID

    @Column(name = "start_time")
    private LocalDateTime startTime; // 起始时间

    @Column(name = "end_time")
    private LocalDateTime endTime; // 终止时间

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间（自动填充，不可修改）

    @Column(name = "status", nullable = false)
    private Integer status; // 状态 1=未分配，2=未开始，3=进行中，4=验收中，5=已打回，6=已完成

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt; // 分配时间

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt; // 提交时间

    @Column(name = "completed_at")
    private LocalDateTime completedAt; // 完成时间

    @Column(name = "rejection_count", nullable = false)
    private Integer rejectionCount; // 打回次数

    @Column(name = "comment", length = 500)
    private String comment; // 当前批语

    @Column(name = "contact_email", length = 100)
    private String contactEmail; // 联系邮箱

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted; // 逻辑删除标志

    // 无参构造
    public Tesk() {
    }

    /* 全参构造，创建任务时传入必要的字段，其他字段在构造函数内设置默认值 */
    public Tesk(String name, String description, Long creatorUserId, LocalDateTime startTime, LocalDateTime endTime,
            String comment, String contactEmail) {
        /* 全参构造，创建任务时传入必要的字段，其他字段在构造函数内设置默认值 */
        this.name = name;
        this.description = description;
        this.creatorUserId = creatorUserId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.contactEmail = contactEmail;
        this.status = 1; // 初始状态为未分配
        this.createdAt = LocalDateTime.now(); // 创建时自动设置创建时间
        this.assignmentUserId = null; // 默认未分配给任何用户
        this.rejectionCount = 0; // 初始打回次数为0
        this.isDeleted = false; // 默认未删除
    }

    // 分配任务
    /* manager 分配任务时，assignmentUserId 更新为被分配的用户ID，status 变为 2=未开始，记录分配时间 */
    public void assignToUser(Long userId) {

        this.assignmentUserId = userId;
        this.status = 2; // 未开始
        this.assignedAt = LocalDateTime.now(); // 记录分配时间
    }

    /* 更新任务信息时，允许修改 name、description、startTime、endTime，其他字段不允许修改 */
    public void updateFrom(Tesk other) {

        this.name = other.name;
        this.description = other.description;
        this.startTime = other.startTime;
        this.endTime = other.endTime;
    }

    // 提交验收
    public void submitForReview(String comment) {
        /** member 提交验收时，status 变为 4=验收中，comment 更新为提交时的批语，记录提交时间 */
        this.comment = comment;
        this.status = 4; // 验收中
        this.submittedAt = LocalDateTime.now(); // 记录提交时间
    }

    // 完成任务
    public void markAsCompleted() {
        /** 任务完成时，status 变为 6=已完成，记录完成时间 */
        this.status = 6; // 已完成
        this.completedAt = LocalDateTime.now(); // 记录完成时间
    }

    // 打回任务
    public void rejectTask() {
        /** 任务被打回时，status 变为 5=已打回，打回次数加1 */
        this.status = 5; // 已打回
        this.rejectionCount++; // 打回次数加1
    }

    // 逻辑删除
    public void markDeleted() {
        this.isDeleted = true;
    }

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public Long getAssignmentUserId() {
        return assignmentUserId;
    }

    public void setAssignmentUserId(Long assignmentUserId) {
        this.assignmentUserId = assignmentUserId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getRejectionCount() {
        return rejectionCount;
    }

    public void setRejectionCount(Integer rejectionCount) {
        this.rejectionCount = rejectionCount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
