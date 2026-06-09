package vesselems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Dialog;

@Repository
public interface DialogRepository extends JpaRepository<Dialog, Long> {
}