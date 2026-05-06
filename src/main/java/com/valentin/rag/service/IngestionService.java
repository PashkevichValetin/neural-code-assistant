package com.valentin.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final PgVectorEmbeddingStore embeddingStore;
    private final dev.langchain4j.model.embedding.EmbeddingModel embeddingModel;

    @EventListener(ApplicationReadyEvent.class)
    public void importDocuments() {
        Path documentsPath = Paths.get("documents");

        DocumentParser parser = new ApacheTikaDocumentParser();
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(documentsPath, parser);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(new DocumentByParagraphSplitter(500, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        System.out.println("Начинаю индексацию документов из: " + documentsPath.toAbsolutePath());
        ingestor.ingest(documents);
        System.out.println("Индексация завершена успешно!");
    }
}