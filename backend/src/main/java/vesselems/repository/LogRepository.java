package vesselems.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<Log> findByModuleOrderByCreateTimeDesc(String module);

    List<Log> findByOperationOrderByCreateTimeDesc(String operation);

    List<Log> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime start, LocalDateTime end);

    List<Log> findTop10ByOrderByCreateTimeDesc();
}
