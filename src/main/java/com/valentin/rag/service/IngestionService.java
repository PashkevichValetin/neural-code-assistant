package com.valentin.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);

    @Value("${documents.path:documents}")
    private String documentsPath;

    private final PgVectorEmbeddingStore embeddingStore;
    private final dev.langchain4j.model.embedding.EmbeddingModel embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void importDocuments() {
        Path path = Paths.get(documentsPath);

        try {
            DocumentParser parser = new TextDocumentParser();
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(path, parser);

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(new DocumentByParagraphSplitter(500, 100))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            logger.info("Начинаю индексацию документов из: {}", path.toAbsolutePath());
            ingestor.ingest(documents);
            logger.info("Индексация завершена успешно!");
        } catch (Exception e) {
            logger.error("Ошибка при индексации документов: {}", e.getMessage(), e);
        }
    }
}