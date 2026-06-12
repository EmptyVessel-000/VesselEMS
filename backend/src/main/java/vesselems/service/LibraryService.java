package vesselems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vesselems.model.Document;
import vesselems.model.Library;
import vesselems.repository.AnnotationRepository;
import vesselems.repository.DocumentRepository;
import vesselems.repository.LibraryRepository;

@Service
public class LibraryService {

    private final LibraryRepository libRepo;
    private final DocumentRepository docRepo;
    private final AnnotationRepository annotationRepo;
    private final VectorStoreService vectorStore;

    public LibraryService(LibraryRepository libRepo, DocumentRepository docRepo,
                          AnnotationRepository annotationRepo, VectorStoreService vectorStore) {
        this.libRepo = libRepo;
        this.docRepo = docRepo;
        this.annotationRepo = annotationRepo;
        this.vectorStore = vectorStore;
    }

    public List<Library> list() {
        return libRepo.findAll();
    }

    public Library getById(Long id) {
        return libRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文库不存在"));
    }

    public Library create(Library lib) {
        lib.setCreateTime(LocalDateTime.now());
        if (lib.getStatus() == null) lib.setStatus(1);
        return libRepo.save(lib);
    }

    public Library update(Long id, Library lib) {
        Library exist = getById(id);
        if (lib.getName() != null) exist.setName(lib.getName());
        if (lib.getDescription() != null) exist.setDescription(lib.getDescription());
        if (lib.getStatus() != null) exist.setStatus(lib.getStatus());
        return libRepo.save(exist);
    }

    @Transactional
    public void delete(Long id) {
        List<Document> docs = docRepo.findByLibraryIdOrderByCreateTimeDesc(id);
        for (Document doc : docs) {
            vectorStore.deleteByDocumentId(doc.getId());
            annotationRepo.deleteByDocumentId(doc.getId());
        }
        docRepo.deleteByLibraryId(id);
        libRepo.deleteById(id);
    }
}