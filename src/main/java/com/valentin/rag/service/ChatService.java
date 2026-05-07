package com.valentin.rag.service;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final SyncAssistant syncAssistant;
    private final ModelSelectorService modelSelectorService;
    private final ContentRetriever contentRetriever;

    public String ask(String question) {
        log.info("Обработка запроса: {}", question);
        return askWithModel(question, null);
    }

    public String askWithModel(String question, String modelName) {
        if (question == null || question.trim().isEmpty()) {
            log.info("Попытка обработки пустого запроса");
            throw new IllegalArgumentException("Вопрос не может быть пустым");
        }

        if (modelName == null || modelName.isEmpty()) {
            log.info("Вызов ассистента без указания модели");
            return syncAssistant.chat(question);
        }

        log.info("Вызов ассистента с моделью: {}", modelName);
        var model = modelSelectorService.getChatLanguageModel(modelName);
        var tempAssistant = dev.langchain4j.service.AiServices.builder(SyncAssistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build();
        return tempAssistant.chat(question);
    }
}