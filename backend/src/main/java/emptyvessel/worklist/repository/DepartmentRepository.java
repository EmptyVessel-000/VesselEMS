package emptyvessel.worklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emptyvessel.worklist.model.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByParentIdOrderBySortOrder(Long parentId);

    List<Department> findByStatusOrderBySortOrder(Integer status);
}