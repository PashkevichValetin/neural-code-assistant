package com.valentin.rag.config;

import com.valentin.rag.service.Assistant;
import com.valentin.rag.service.SyncAssistant;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class LangChainConfig {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.default-model}")
    private String defaultModelName;

    @Bean
    public PgVectorEmbeddingStore embeddingStore() {
        log.info("Создание PgVectorEmbeddingStore с параметрами: host=localhost, port=5432, database=rag_db, table=embeddings");
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
    public ChatLanguageModel chatLanguageModel() {
        log.info("Создание ChatLanguageModel для модели: {}", defaultModelName);
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(defaultModelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("Создание StreamChatLanguageModel для модели: {}", defaultModelName);
        return OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(defaultModelName)
                .temperature(0.1)
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Создание EmbeddingModel для модели: nomic-embed-text");
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName("nomic-embed-text")
                .build();
    }

    @Bean
    public ContentRetriever contentRetriever(PgVectorEmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
        log.info("Создание ContentRetriever");
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .build();
    }

    @Bean
    public Assistant streamingAssistant(StreamingChatLanguageModel streamingChatLanguageModel,
                                        ContentRetriever contentRetriever) {
        log.info("Создание StreamingAssistant");
        return AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }

    @Bean
    public SyncAssistant syncAssistant(ChatLanguageModel chatLanguageModel, ContentRetriever contentRetriever) {
        log.info("Создание SyncAssistant");
        return AiServices.builder(SyncAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}