package emptyvessel.worklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 100, unique = true)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    public Role() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getModifyTime() { return modifyTime; }
    public void setModifyTime(LocalDateTime modifyTime) { this.modifyTime = modifyTime; }
}