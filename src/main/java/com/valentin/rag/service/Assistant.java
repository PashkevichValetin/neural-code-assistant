package com.valentin.rag.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

public interface Assistant {
    @SystemMessage("Ты — опытный Java-разработчик. Отвечай на вопросы, используя только предоставленный контекст.")
    TokenStream chat(String message);

    @SystemMessage("Ты — опытный Java-разработчик. Отвечай на вопросы, используя только предоставленный контекст.")
    TokenStream chatWithModel(String message, String modelName);
}