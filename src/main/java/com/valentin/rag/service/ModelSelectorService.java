package com.valentin.rag.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ModelSelectorService {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.default-model}")
    private String defaultModelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatLanguageModel getChatLanguageModel(String modelName) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel(String modelName) {
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    public EmbeddingModel getEmbeddingModel(String modelName) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }

    public List<String> getAvailableModels() {
        try {
            String url = baseUrl + "/api/tags";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("models")) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");

                return models.stream()
                        .map(m -> (String) m.get("name"))
                        .filter(name -> !name.contains("embed"))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при получении моделей из Ollama: " + e.getMessage());
        }
        return List.of(defaultModelName);
    }
}





































