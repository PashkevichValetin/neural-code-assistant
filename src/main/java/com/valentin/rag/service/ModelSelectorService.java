package com.valentin.rag.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelSelectorService {


    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.default-model}")
    private String defaultModelName;

    private final RestTemplate restTemplate = new RestTemplate();

    public ChatLanguageModel getChatLanguageModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            log.warn("Попытка получения модели с пустым именем");
            throw new IllegalArgumentException("Имя модели не может быть пустым");
        }
        log.info("Создание ChatLanguageModel для модели: {}", modelName);
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(5))
                .numCtx(16384)
                .numPredict(4096)
                .build();
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            log.warn("Попытка получения модели с пустым именем");
            throw new IllegalArgumentException("Имя модели не может быть пустым");
        }
        log.info("Создание StreamingChatLanguageModel для модели: {}", modelName);
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(5))
                .numCtx(16384)
                .numPredict(4096)
                .build();
    }

    public List<String> getAvailableModels() {
        try {
            log.info("Получение списка доступных моделей из Ollama");
            String url = baseUrl + "/api/tags";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("models")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
                List<String> result = models.stream()
                        .map(m -> (String) m.get("name"))
                        .filter(name -> !name.contains("embed"))
                        .collect(Collectors.toList());
                log.info("Найдено {} доступных моделей", result.size());
                return result;
            }
        } catch (Exception e) {
            log.error("Ошибка при получении моделей из Ollama: {}", e.getMessage(), e);
        }
        log.warn("Возвращена модель по умолчанию: {}", defaultModelName);
        return List.of(defaultModelName);
    }
}



























