package vesselems.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import vesselems.model.Model;

@Service
public class LLMService {

    private final RestTemplate rest;

    public LLMService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000);
        this.rest = new RestTemplate(factory);
    }

    public String chat(Model model, String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));
        body.put("temperature", 0.5);
        body.put("max_tokens", 3000);
        return doRequest(model, body);
    }

    @SuppressWarnings("unchecked")
    public String chatMulti(Model model, List<Map<String, String>> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());
        body.put("messages", messages);
        body.put("temperature", 0.1);
        body.put("max_tokens", 300);
        return doRequest(model, body);
    }

    @SuppressWarnings("unchecked")
    public float[] embedding(Model model, String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());
        body.put("input", text);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + model.getApiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map<String, Object> resp = rest.postForObject(
                model.getApiUrl() + "/embeddings", entity, Map.class);

        if (resp == null) {
            throw new RuntimeException("Embedding模型无响应");
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Embedding模型返回无data");
        }

        List<Double> embeddingList = (List<Double>) data.get(0).get("embedding");
        if (embeddingList == null) {
            throw new RuntimeException("Embedding模型返回无embedding");
        }

        float[] result = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            result[i] = embeddingList.get(i).floatValue();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public String chatVision(Model model, List<Map<String, Object>> imageMessages,
                              String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model.getModelId());

        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");

        List<Map<String, Object>> contentParts = new java.util.ArrayList<>();
        if (prompt != null && !prompt.isEmpty()) {
            contentParts.add(Map.of("type", "text", "text", prompt));
        }
        contentParts.addAll(imageMessages);

        userMsg.put("content", contentParts);
        messages.add(userMsg);

        body.put("messages", messages);
        body.put("temperature", 0.3);
        body.put("max_tokens", 2000);

        return doRequest(model, body);
    }

    @SuppressWarnings("unchecked")
    private String doRequest(Model model, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + model.getApiKey());
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map<String, Object> resp = rest.postForObject(
                model.getApiUrl() + "/chat/completions", entity, Map.class);

        if (resp == null) {
            throw new RuntimeException("大模型无响应");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("大模型返回无choices");
        }

        Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
        if (msg == null) {
            throw new RuntimeException("大模型返回无message");
        }

        return (String) msg.get("content");
    }
}