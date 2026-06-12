package vesselems.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.service.RAGService;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private final RAGService ragService;

    public RAGController(RAGService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/query")
    public ApiResponse<Map<String, Object>> query(@RequestBody Map<String, Object> body) {
        Long libraryId = Long.valueOf(body.get("libraryId").toString());
        String question = (String) body.get("question");
        Long modelId = Long.valueOf(body.get("modelId").toString());
        return ApiResponse.success(ragService.query(libraryId, question, modelId));
    }
}