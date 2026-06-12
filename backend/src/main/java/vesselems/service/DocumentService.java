package vesselems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vesselems.model.Document;
import vesselems.model.Library;
import vesselems.repository.AnnotationRepository;
import vesselems.repository.DocumentRepository;
import vesselems.repository.LibraryRepository;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository docRepo;
    private final AnnotationRepository annotationRepo;
    private final LibraryRepository libRepo;
    private final VectorStoreService vectorStore;
    private final DocumentProcessService processService;

    public DocumentService(DocumentRepository docRepo, AnnotationRepository annotationRepo,
                           LibraryRepository libRepo, VectorStoreService vectorStore,
                           DocumentProcessService processService) {
        this.docRepo = docRepo;
        this.annotationRepo = annotationRepo;
        this.libRepo = libRepo;
        this.vectorStore = vectorStore;
        this.processService = processService;
    }

    public Document upload(MultipartFile file, Long libraryId) {
        Library lib = libRepo.findById(libraryId)
                .orElseThrow(() -> new IllegalArgumentException("文库不存在"));

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName).toLowerCase();

        Document doc = new Document();
        doc.setLibraryId(libraryId);
        doc.setFileName(originalName != null ? originalName : "unknown");
        doc.setFileType(ext);
        doc.setFileSize(file.getSize());
        doc.setStatus(0);
        doc.setCreateTime(LocalDateTime.now());
        doc = docRepo.save(doc);

        try {
            byte[] fileBytes = file.getBytes();
            // 调用独立 Bean 的方法，@Async 会生效
            processService.processAsync(doc.getId(), fileBytes, ext);
        } catch (Exception e) {
            log.error("文件上传后处理失败: libraryId={}, fileName={}", libraryId, originalName, e);
            doc.setStatus(-1);
            docRepo.save(doc);
        }

        return doc;
    }

    @Transactional
    public void delete(Long documentId) {
        vectorStore.deleteByDocumentId(documentId);
        annotationRepo.deleteByDocumentId(documentId);
        docRepo.deleteById(documentId);
    }

    public List<Document> listByLibrary(Long libraryId) {
        return docRepo.findByLibraryIdOrderByCreateTimeDesc(libraryId);
    }

    private String getExtension(String filename) {
        if (filename == null) return "unknown";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "unknown";
    }
}