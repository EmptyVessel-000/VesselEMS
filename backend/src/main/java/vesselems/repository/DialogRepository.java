package vesselems.repository;

<<<<<<< HEAD
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
=======
import org.springframework.data.jpa.repository.JpaRepository;
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
import org.springframework.stereotype.Repository;

import vesselems.model.Dialog;

@Repository
public interface DialogRepository extends JpaRepository<Dialog, Long> {
<<<<<<< HEAD

    List<Dialog> findBySessionIdOrderByCreateTimeAsc(String sessionId);

    @Query(value = "SELECT d.session_id, COUNT(*) AS cnt, MIN(d.create_time) AS first_time, " +
            "SUBSTRING((SELECT d2.content FROM dialog d2 WHERE d2.session_id = d.session_id ORDER BY d2.create_time ASC LIMIT 1), 1, 200) AS first_question " +
            "FROM dialog d GROUP BY d.session_id ORDER BY first_time DESC", nativeQuery = true)
    List<Object[]> findSessions();
=======
>>>>>>> 2609d393650989f717325435186f2346d621f4dc
}