package vesselems.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vesselems.common.ApiResponse;
import vesselems.model.Annotation;
import vesselems.model.Document;
import vesselems.repository.AnnotationRepository;
import vesselems.service.DocumentService;
import vesselems.service.LibraryService;

@RestController
@RequestMapping("/api/library/{libId}/document")
public class DocumentController {

    private final DocumentService documentService;
    private final LibraryService libraryService;
    private final AnnotationRepository annotationRepo;

    public DocumentController(DocumentService documentService, LibraryService libraryService,
                              AnnotationRepository annotationRepo) {
        this.documentService = documentService;
        this.libraryService = libraryService;
        this.annotationRepo = annotationRepo;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long libId) {
        String libName = libraryService.getById(libId).getName();
        List<Document> docs = documentService.listByLibrary(libId);

        List<Map<String, Object>> result = docs.stream().map(doc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", doc.getId());
            m.put("libraryId", doc.getLibraryId());
            m.put("libraryName", libName);
            m.put("fileName", doc.getFileName());
            m.put("fileType", doc.getFileType());
            m.put("fileSize", doc.getFileSize());
            m.put("status", doc.getStatus());
            m.put("createTime", doc.getCreateTime());

            List<Annotation> annotations = annotationRepo
                    .findByDocumentIdOrderByAnnotationIndexAsc(doc.getId());
            m.put("annotationCount", annotations.size());
            m.put("annotations", annotations);
            return m;
        }).toList();

        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> upload(@PathVariable Long libId,
                                                    @RequestParam("file") MultipartFile file) {
        Document doc = documentService.upload(file, libId);
        Map<String, Object> result = new HashMap<>();
        result.put("id", doc.getId());
        result.put("status", doc.getStatus());
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{docId}")
    public ApiResponse<Void> delete(@PathVariable Long docId) {
        documentService.delete(docId);
        return ApiResponse.success(null);
    }
}