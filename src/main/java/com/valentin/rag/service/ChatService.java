package com.valentin.rag.service;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SyncAssistant syncAssistant;       // синхронный ассистент (по умолчанию)
    private final ModelSelectorService modelSelectorService;
    private final ContentRetriever contentRetriever; // общий ретривер

    public String ask(String question) {
        return askWithModel(question, null);
    }

    public String askWithModel(String question, String modelName) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Вопрос не может быть пустым");
        }

        if (modelName == null || modelName.isEmpty()) {
            return syncAssistant.chat(question);
        }

        // для указанной модели создаём временный синхронный ассистент
        var model = modelSelectorService.getChatLanguageModel(modelName);
        var tempAssistant = dev.langchain4j.service.AiServices.builder(SyncAssistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build();
        return tempAssistant.chat(question);
    }
}