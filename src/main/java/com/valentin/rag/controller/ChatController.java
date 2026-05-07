package com.valentin.rag.controller;

import com.valentin.rag.service.Assistant;
import com.valentin.rag.service.ChatService;
import com.valentin.rag.service.ModelSelectorService;
import com.valentin.rag.service.SecurityService;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final Assistant streamingAssistant;
    private final ModelSelectorService modelSelectorService;
    private final ContentRetriever contentRetriever;
    private final SecurityService securityService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> stream(@RequestParam String question,
                                            @RequestParam(required = false) String modelName) {
        log.info("Старт стримингового запроса: {}", question);

        if (!securityService.isUserAuthenticated()) {
            log.warn("Попытка доступа к стриминговому API без аутентификации");
            return Flux.error(new SecurityException("Доступ запрещен. Пожалуйста, авторизуйтесь"));
        }

        if (question == null || question.trim().isEmpty()) {
            log.warn("Попытка выполнить пустой запрос");
            return Flux.error(new IllegalArgumentException("Вопрос не может быть пустым"));
        }

        return Flux.create(emitter -> {
            try {
                if (modelName == null || modelName.isEmpty()) {
                    log.info("Вызов streamingAssistant без указания модели");
                    streamingAssistant.chat(question)
                            .onNext(token -> emitter.next(Map.of("content", token)))
                            .onComplete(response -> {
                                log.info("Стриминг завершен");
                                emitter.complete();
                            })
                            .onError(emitter::error)
                            .start();
                } else {
                    log.info("Вызов streamingAssistant с моделью: {}", modelName);
                    var model = modelSelectorService.getStreamingChatLanguageModel(modelName);
                    var tempAssistant = AiServices.builder(Assistant.class)
                            .streamingChatLanguageModel(model)
                            .contentRetriever(contentRetriever)
                            .build();

                    tempAssistant.chat(question)
                            .onNext(token -> emitter.next(Map.of("content", token)))
                            .onComplete(response -> {
                                log.info("Стриминг с моделью {} завершен", modelName);
                                emitter.complete();
                            })
                            .onError(emitter::error)
                            .start();
                }
            } catch (Exception e) {
                log.error("Ошибка в стриминговом вызове: {}", e.getMessage(), e);
                emitter.error(e);
            }
        });
    }

    @GetMapping("/ask")
    public ResponseEntity<String> ask(@RequestParam String question,
                                      @RequestParam(required = false) String modelName) {
        log.info("Старт синхронного запроса: {}", question);
        if (!securityService.isUserAuthenticated()) {
            log.warn("Попытка доступа к синхронному API без аутентификации");
            return ResponseEntity.status(401).body("Доступ запрещен. Пожалуйста, авторизуйтесь");
        }

        if (question == null || question.trim().isEmpty()) {
            log.warn("Попытка выполнить пустой запрос");
            return ResponseEntity.badRequest().body("Вопрос не может быть пустым");
        }
        try {
            String answer = (modelName == null || modelName.isEmpty())
                    ? chatService.ask(question)
                    : chatService.askWithModel(question, modelName);
            log.info("Ответ сгенерирован: {}", answer.substring(0, Math.min(50, answer.length())) + "...");
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка при обработке запроса: " + e.getMessage());
        }
    }

    @GetMapping("/models")
    public ResponseEntity<List<String>> getAvailableModels() {
        log.info("Получение списка доступных моделей");
        if (!securityService.isUserAuthenticated()) {
            log.warn("Попытка доступа к списку моделей без аутентификации");
            return ResponseEntity.status(401).build();
        }

        try {
            List<String> models = modelSelectorService.getAvailableModels();
            log.info("Возвращено {} моделей", models.size());
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            log.error("Ошибка при получении списка моделей: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}