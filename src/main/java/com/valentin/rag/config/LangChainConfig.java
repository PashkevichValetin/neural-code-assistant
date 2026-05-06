package com.valentin.rag.config;

import com.valentin.rag.service.Assistant;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChainConfig {

    @Bean
    public PgVectorEmbeddingStore embeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("rag_db")
                .user("valentin")
                .password("secret")
                .table("embeddings")
                .dimension(768)
                .build();
    }

    @Bean
    public Assistant assistant(StreamingChatLanguageModel streamingChatModel,
                               PgVectorEmbeddingStore embeddingStore,
                               EmbeddingModel embeddingModel) {

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .build();

        return AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}