package com.hypermall.ai.client;

import java.util.List;

public interface AiClient {

    String chat(List<Message> messages, String systemPrompt);

    List<Float> getTextEmbedding(String text);

    List<Float> getImageEmbedding(String imageUrl);

    String getProviderName();

    record Message(String role, String content) {}
}
