package vesselems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByLibraryIdOrderByCreateTimeDesc(Long libraryId);
    void deleteByLibraryId(Long libraryId);
}