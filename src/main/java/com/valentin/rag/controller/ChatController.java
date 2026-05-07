package com.valentin.rag.controller;

import com.valentin.rag.service.Assistant;
import com.valentin.rag.service.ChatService;
import com.valentin.rag.service.ModelSelectorService;
import com.valentin.rag.service.SecurityService;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final Assistant streamingAssistant;
    private final ModelSelectorService modelSelectorService;
    private final ContentRetriever contentRetriever;
    private final SecurityService securityService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> stream(@RequestParam String question,
                                            @RequestParam(required = false) String modelName) {
        // Проверяем аутентификацию
        if (!securityService.isUserAuthenticated()) {
            return Flux.error(new SecurityException("Доступ запрещен. Пожалуйста, авторизуйтесь"));
        }

        if (question == null || question.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("Вопрос не может быть пустым"));
        }

        return Flux.create(emitter -> {
            try {
                // Если модель не указана — используем основной ассистент
                if (modelName == null || modelName.isEmpty()) {
                    streamingAssistant.chat(question)
                            .onNext(token -> emitter.next(Map.of("content", token)))
                            .onComplete(response -> emitter.complete())
                            .onError(emitter::error)
                            .start();
                } else {
                    // Для указанной модели создаём временный ассистент
                    var model = modelSelectorService.getStreamingChatLanguageModel(modelName);
                    var tempAssistant = AiServices.builder(Assistant.class)
                            .streamingChatLanguageModel(model)
                            .contentRetriever(contentRetriever)
                            .build();

                    tempAssistant.chat(question)
                            .onNext(token -> emitter.next(Map.of("content", token)))
                            .onComplete(response -> emitter.complete())
                            .onError(emitter::error)
                            .start();
                }
            } catch (Exception e) {
                emitter.error(e);
            }
        });
    }

    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String question,
                                      @RequestParam(required = false) String modelName) {
        if (!securityService.isUserAuthenticated()) {
            return ResponseEntity.status(401).body("Доступ запрещен. Пожалуйста, авторизуйтесь");
        }

        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Вопрос не может быть пустым");
        }
        try {
            String answer = (modelName == null || modelName.isEmpty())
                    ? chatService.ask(question)
                    : chatService.askWithModel(question, modelName);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при обработке запроса: " + e.getMessage());
        }
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> getAvailableModels() {
        if (!securityService.isUserAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        try {
            return ResponseEntity.ok(modelSelectorService.getAvailableModels());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}