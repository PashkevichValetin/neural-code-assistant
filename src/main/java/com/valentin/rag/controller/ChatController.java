package com.valentin.rag.controller;

import com.valentin.rag.service.Assistant;
import com.valentin.rag.service.ChatService;
import com.valentin.rag.service.ModelSelectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    private final Assistant assistant;
    private final ModelSelectorService modelSelectorService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, String>> stream(@RequestParam String question, @RequestParam(required = false) String modelName) {
        return Flux.create(emitter -> {
            // Если модель не указана, используем текущую
            if (modelName == null || modelName.isEmpty()) {
                assistant.chat(question)
                        .onNext(token -> emitter.next(Map.of("content", token)))
                        .onComplete(tokenResponse -> emitter.complete())
                        .onError(emitter::error)
                        .start();
            } else {
                // Используем указанную модель
                assistant.chatWithModel(question, modelName)
                        .onNext(token -> emitter.next(Map.of("content", token)))
                        .onComplete(tokenResponse -> emitter.complete())
                        .onError(emitter::error)
                        .start();
            }
        });
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question, @RequestParam(required = false) String modelName) {
        if (question == null || question.trim().isEmpty()) {
            return "Вопрос не может быть пустым";
        }

        if (modelName == null || modelName.isEmpty()) {
            return chatService.ask(question);
        } else {
            return chatService.askWithModel(question, modelName);
        }
    }

    @GetMapping("/models")
    public List<String> getAvailableModels() {
        return modelSelectorService.getAvailableModels();
    }
}
