package com.valentin.rag.service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatLanguageModel chatModel;
    private final PgVectorEmbeddingStore embeddingStore;
    private final dev.langchain4j.model.embedding.EmbeddingModel embeddingModel;

    public String ask(String question) {
        EmbeddingStoreRetriever retriever = EmbeddingStoreRetriever.from(
                embeddingStore,
                embeddingModel,
                3
        );

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                .chatLanguageModel(chatModel)
                .retriever(retriever)
                .build();

        return chain.execute(question);
    }
}