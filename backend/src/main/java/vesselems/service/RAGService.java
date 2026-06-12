package vesselems.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import vesselems.model.Model;
import vesselems.repository.ModelRepository;
import vesselems.service.VectorStoreService.SearchResult;

@Service
public class RAGService {

    private final ModelRepository modelRepo;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStore;
    private final LLMService llmService;

    private static final int TOP_K = 5;

    public RAGService(ModelRepository modelRepo, EmbeddingService embeddingService,
                      VectorStoreService vectorStore, LLMService llmService) {
        this.modelRepo = modelRepo;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.llmService = llmService;
    }

    public Map<String, Object> query(Long libraryId, String question, Long modelId) {
        Model chatModel = modelRepo.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));

        float[] queryVec = embeddingService.embed(question);
        List<SearchResult> results = vectorStore.search(queryVec, libraryId, TOP_K);

        String prompt = buildPrompt(question, results);
        String answer = llmService.chat(chatModel, prompt);

        List<Map<String, Object>> references = new ArrayList<>();
        for (SearchResult sr : results) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("annotationId", sr.getAnnotationId());
            ref.put("content", sr.getContent());
            ref.put("score", Math.round(sr.getSimilarity() * 10000.0) / 10000.0);
            references.add(ref);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer);
        result.put("references", references);
        return result;
    }

    private String buildPrompt(String question, List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是知识库问答助手。请根据以下参考资料回答用户问题。");
        sb.append("如果参考资料中没有相关信息，请如实告知。\n\n");
        sb.append("参考资料：\n");

        for (int i = 0; i < results.size(); i++) {
            sb.append("[").append(i + 1).append("] ")
              .append(results.get(i).getContent()).append("\n\n");
        }

        sb.append("用户问题：").append(question);
        return sb.toString();
    }
}