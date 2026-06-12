package vesselems.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import vesselems.model.Annotation;
import vesselems.model.Document;
import vesselems.model.Model;
import vesselems.repository.AnnotationRepository;
import vesselems.repository.DocumentRepository;
import vesselems.repository.ModelRepository;

@Service
public class DocumentProcessService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessService.class);

    private static final List<String> TEXT_TYPES = List.of("txt", "pdf", "docx", "doc", "md", "markdown");
    private static final List<String> IMAGE_TYPES = List.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    private final DocumentRepository docRepo;
    private final AnnotationRepository annotationRepo;
    private final ModelRepository modelRepo;
    private final DocumentChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStore;
    private final LLMService llmService;

    public DocumentProcessService(DocumentRepository docRepo, AnnotationRepository annotationRepo,
                                   ModelRepository modelRepo, DocumentChunkingService chunkingService,
                                   EmbeddingService embeddingService, VectorStoreService vectorStore,
                                   LLMService llmService) {
        this.docRepo = docRepo;
        this.annotationRepo = annotationRepo;
        this.modelRepo = modelRepo;
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.llmService = llmService;
    }

    @Async("documentProcessExecutor")
    public void processAsync(Long documentId, byte[] fileBytes, String fileType) {
        Document doc = docRepo.findById(documentId).orElse(null);
        if (doc == null) return;

        try {
            doc.setStatus(1);
            docRepo.save(doc);

            if (TEXT_TYPES.contains(fileType)) {
                processText(doc, fileBytes, fileType);
            } else if (IMAGE_TYPES.contains(fileType)) {
                processImage(doc, fileBytes, fileType);
            }

            doc.setStatus(2);
            docRepo.save(doc);
        } catch (Exception e) {
            log.error("文档处理失败: documentId={}, fileType={}", documentId, fileType, e);
            doc.setStatus(-1);
            docRepo.save(doc);
        }
    }

    private void processText(Document doc, byte[] fileBytes, String fileType) {
        Model embeddingModel = modelRepo.findAll().stream()
                .filter(m -> m.getVersion() != null && m.getVersion() == 1
                        && m.getStatus() != null && m.getStatus() == 1
                        && "EMBEDDING".equals(m.getModelType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("无可用Embedding模型"));

        List<String> chunks = chunkingService.chunk(fileBytes, fileType);

        int index = 0;
        for (String text : chunks) {
            Annotation ann = new Annotation();
            ann.setDocumentId(doc.getId());
            ann.setAnnotationIndex(index);
            ann.setContent(text);
            ann.setModelId(embeddingModel.getId());
            ann.setCreateTime(LocalDateTime.now());
            ann = annotationRepo.save(ann);

            float[] emb = llmService.embedding(embeddingModel, text);
            vectorStore.insert(ann.getId(), emb);
            index++;
        }
    }

    private void processImage(Document doc, byte[] fileBytes, String fileType) {
        Model visionModel = modelRepo.findAll().stream()
                .filter(m -> m.getVersion() != null && m.getVersion() == 1
                        && m.getStatus() != null && m.getStatus() == 1
                        && "VISION".equals(m.getModelType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("无可用Vision模型"));

        String base64 = Base64.getEncoder().encodeToString(fileBytes);
        String mimeType = "image/" + (fileType.equals("jpg") ? "jpeg" : fileType);
        String dataUrl = "data:" + mimeType + ";base64," + base64;

        List<Map<String, Object>> imageMessages = List.of(
                Map.of("type", "image_url",
                        "image_url", Map.of("url", dataUrl))
        );

        String result = llmService.chatVision(visionModel, imageMessages,
                "请详细描述这张图片的内容，包括其中的文字、图表、人物、场景等所有可视信息。");

        Annotation ann = new Annotation();
        ann.setDocumentId(doc.getId());
        ann.setAnnotationIndex(null);
        ann.setContent(result);
        ann.setModelId(visionModel.getId());
        ann.setCreateTime(LocalDateTime.now());
        ann = annotationRepo.save(ann);

        // 图片描述也做 embedding，参与知识问答检索
        float[] emb = embeddingService.embed(result);
        vectorStore.insert(ann.getId(), emb);
    }
}