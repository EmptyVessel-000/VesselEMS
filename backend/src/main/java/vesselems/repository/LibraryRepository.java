package vesselems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Library;

@Repository
public interface LibraryRepository extends JpaRepository<Library, Long> {
}