package com.valentin.rag.service;

import dev.langchain4j.service.SystemMessage;

public interface SyncAssistant {

    @SystemMessage("Ты — опытный Java-разработчик. Отвечай на вопросы, используя только предоставленный контекст.")
    String chat(String message);
}