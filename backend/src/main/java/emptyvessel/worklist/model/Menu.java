package emptyvessel.worklist.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menu_path", length = 256)
    private String menuPath;

    @Column(name = "menu_component", length = 256)
    private String menuComponent;

    @Column(name = "menu_icon", length = 128)
    private String menuIcon;

    @Column(name = "menu_type")
    private Integer menuType;

    @Column(name = "visible")
    private Integer visible;

    @Column(name = "is_frame")
    private Integer isFrame;

    @Column(name = "permission", length = 128)
    private String permission;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "status")
    private Integer status;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "modify_time")
    private LocalDateTime modifyTime;

    public Menu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getMenuPath() { return menuPath; }
    public void setMenuPath(String menuPath) { this.menuPath = menuPath; }
    public String getMenuComponent() { return menuComponent; }
    public void setMenuComponent(String menuComponent) { this.menuComponent = menuComponent; }
    public String getMenuIcon() { return menuIcon; }
    public void setMenuIcon(String menuIcon) { this.menuIcon = menuIcon; }
    public Integer getMenuType() { return menuType; }
    public void setMenuType(Integer menuType) { this.menuType = menuType; }
    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }
    public Integer getIsFrame() { return isFrame; }
    public void setIsFrame(Integer isFrame) { this.isFrame = isFrame; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getModifyTime() { return modifyTime; }
    public void setModifyTime(LocalDateTime modifyTime) { this.modifyTime = modifyTime; }
}