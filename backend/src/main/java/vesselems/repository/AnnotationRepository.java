package vesselems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vesselems.model.Annotation;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    List<Annotation> findByDocumentIdOrderByAnnotationIndexAsc(Long documentId);
    void deleteByDocumentId(Long documentId);
}