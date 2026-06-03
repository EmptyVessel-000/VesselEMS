package emptyvessel.worklist.dto;

import java.time.LocalDateTime;

/**
 * 任务 DTO，包含关联的用户名信息
 * 用于前端展示，避免前端逐个查询用户信息
 */
public class TeskWithNamesDto {
    private Long id;
    private String name;
    private String description;
    private Long creatorUserId;
    private String creatorUserName;
    private Long assignmentUserId;
    private String assignmentUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Integer status;
    private LocalDateTime assignedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private Integer rejectionCount;
    private String comment;
    private String contactEmail;
    private Boolean isDeleted;

    public TeskWithNamesDto() {
    }

    public TeskWithNamesDto(Long id, String name, String description, Long creatorUserId, String creatorUserName,
            Long assignmentUserId, String assignmentUserName, LocalDateTime startTime, LocalDateTime endTime,
            LocalDateTime createdAt, Integer status, LocalDateTime assignedAt, LocalDateTime submittedAt,
            LocalDateTime completedAt, Integer rejectionCount, String comment, String contactEmail,
            Boolean isDeleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorUserId = creatorUserId;
        this.creatorUserName = creatorUserName;
        this.assignmentUserId = assignmentUserId;
        this.assignmentUserName = assignmentUserName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.status = status;
        this.assignedAt = assignedAt;
        this.submittedAt = submittedAt;
        this.completedAt = completedAt;
        this.rejectionCount = rejectionCount;
        this.comment = comment;
        this.contactEmail = contactEmail;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
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

    public String getCreatorUserName() {
        return creatorUserName;
    }

    public void setCreatorUserName(String creatorUserName) {
        this.creatorUserName = creatorUserName;
    }

    public Long getAssignmentUserId() {
        return assignmentUserId;
    }

    public void setAssignmentUserId(Long assignmentUserId) {
        this.assignmentUserId = assignmentUserId;
    }

    public String getAssignmentUserName() {
        return assignmentUserName;
    }

    public void setAssignmentUserName(String assignmentUserName) {
        this.assignmentUserName = assignmentUserName;
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