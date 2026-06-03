package emptyvessel.worklist.service;

import java.util.List;

import org.springframework.stereotype.Service;

import emptyvessel.worklist.model.Department;
import emptyvessel.worklist.repository.DepartmentRepository;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> listDepartments() {
        return departmentRepository.findAll();
    }

    public List<Department> listByParentId(Long parentId) {
        return departmentRepository.findByParentIdOrderBySortOrder(parentId);
    }

    public List<Department> listEnabled() {
        return departmentRepository.findByStatusOrderBySortOrder(1);
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + id));
    }

    public Department createDepartment(Department department) {
        department.setStatus(department.getStatus() != null ? department.getStatus() : 1);
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department updated) {
        Department department = getDepartmentById(id);
        if (updated.getDeptName() != null) department.setDeptName(updated.getDeptName());
        if (updated.getParentId() != null) department.setParentId(updated.getParentId());
        if (updated.getLeader() != null) department.setLeader(updated.getLeader());
        if (updated.getPhone() != null) department.setPhone(updated.getPhone());
        if (updated.getEmail() != null) department.setEmail(updated.getEmail());
        if (updated.getSortOrder() != null) department.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null) department.setStatus(updated.getStatus());
        return departmentRepository.save(department);
    }

    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("部门不存在: " + id);
        }
        departmentRepository.deleteById(id);
    }
}