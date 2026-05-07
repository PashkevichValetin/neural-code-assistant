package com.valentin.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    @Value("${documents.path:documents}")
    private String documentsPath;

    private final PgVectorEmbeddingStore embeddingStore;
    private final dev.langchain4j.model.embedding.EmbeddingModel embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void importDocuments() {
        Path path = Paths.get(documentsPath);

        try {
            log.info("Начинаю индексацию документов из: {}", path.toAbsolutePath());
            DocumentParser parser = new TextDocumentParser();
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(path, parser);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(new DocumentByParagraphSplitter(500, 100))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(documents);
            log.info("Индексация завершена успешно!");
        } catch (Exception e) {
            log.error("Ошибка при индексации документов: {}", e.getMessage(), e);
        }
    }
}