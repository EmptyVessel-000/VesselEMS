package vesselems.service;

import org.springframework.stereotype.Service;

import vesselems.model.Model;
import vesselems.repository.ModelRepository;

@Service
public class EmbeddingService {

    private final ModelRepository modelRepo;
    private final LLMService llmService;

    public EmbeddingService(ModelRepository modelRepo, LLMService llmService) {
        this.modelRepo = modelRepo;
        this.llmService = llmService;
    }

    public float[] embed(String text) {
        Model model = modelRepo.findAll().stream()
                .filter(m -> m.getVersion() != null && m.getVersion() == 1
                        && m.getStatus() != null && m.getStatus() == 1
                        && "EMBEDDING".equals(m.getModelType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("无可用Embedding模型(version=1, model_type=EMBEDDING, status=1)"));
        return llmService.embedding(model, text);
    }
}