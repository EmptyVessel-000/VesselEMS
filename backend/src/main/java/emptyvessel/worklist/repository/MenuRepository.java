package emptyvessel.worklist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import emptyvessel.worklist.model.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByParentIdOrderBySortOrder(Long parentId);

    List<Menu> findByStatusOrderBySortOrder(Integer status);
}