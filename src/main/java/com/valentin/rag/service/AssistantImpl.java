package com.valentin.rag.service;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssistantImpl implements  Assistant {

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ContentRetriever contentRetriever;
    private final ModelSelectorService modelSelectorService;

    @Override
    public TokenStream chat(String message) {
        return AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .contentRetriever(contentRetriever)
                .build()
                .chat(message);
    }

    @Override
    public TokenStream chatWithModel(String message, String modelName) {

        StreamingChatLanguageModel model = modelSelectorService.getStreamingChatLanguageModel(modelName);

        return AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build()
                .chat(message);
    }
}
