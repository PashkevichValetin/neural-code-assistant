package com.valentin.rag.controller;

import com.valentin.rag.service.Assistant;
import com.valentin.rag.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final Assistant assistant;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String question) {
        if (question == null || question.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("Вопрос не может быть пустым"));
        }

        return Flux.create(emitter -> {
            assistant.chat(question)
                    .onNext(emitter::next)
                    .onComplete(tokenResponse -> emitter.complete())
                    .onError(emitter::error)
                    .start();
        });
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Вопрос не может быть пустым";
        }
        return chatService.ask(question);
    }
}