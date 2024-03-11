/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.spring.demo.ai.playground.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * * @author Christian Tzolov
 */
@Service
public class CustomerSupportAgent {

    private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAgent.class);

    private static final int CHAT_HISTORY_WINDOW_SIZE = 40;

    @Value("classpath:/prompt/system-qa.st")
    private Resource systemPrompt;

    private final StreamingChatClient chatClient;

    private final VectorStore vectorStore;

    private final ChatHistory chatHistory;

    public CustomerSupportAgent(StreamingChatClient chatClient, VectorStore vectorStore, ChatHistory chatHistory) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.chatHistory = chatHistory;
    }

    public Flux<String> chat(String chatId, String userMessageContent) {

        // Retrieve related documents to query
        List<Document> similarDocuments = this.vectorStore.similaritySearch(userMessageContent);

        Message systemMessage = getSystemMessage(similarDocuments, this.chatHistory.getLastN(chatId, CHAT_HISTORY_WINDOW_SIZE));

        logger.info("System Message: {}", systemMessage.getContent());

        UserMessage userMessage = new UserMessage(userMessageContent);

        this.chatHistory.addMessage(chatId, userMessage);

        // Ask the AI model
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return this.chatClient.stream(prompt).map((ChatResponse chatResponse) -> {

            if (!isValidResponse(chatResponse)) {
                logger.warn("ChatResponse or the result output is null!");
                return "";
            }

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            this.chatHistory.addMessageChunk(chatId, assistantMessage);

            return (assistantMessage.getContent() != null) ? assistantMessage.getContent() : "";
        });
    }

    private boolean isValidResponse(ChatResponse chatResponse) {
        return chatResponse != null && chatResponse.getResult() != null
                && chatResponse.getResult().getOutput() != null;
    }

    private Message getSystemMessage(List<Document> similarDocuments, List<Message> conversationHistory) {

        String history = conversationHistory.stream().map(m -> m.getMessageType() + ": " + m.getContent())
                .collect(Collectors.joining(System.lineSeparator()));

        String documents = similarDocuments.stream().map(entry -> entry.getContent())
                .collect(Collectors.joining(System.lineSeparator()));

        // Needs to be created on each call as it is not thread safe.
        Message systemMessage = new SystemPromptTemplate(this.systemPrompt)
                .createMessage(Map.of(
                        "documents", documents,
                        "current_date", java.time.LocalDate.now(),
                        "history", history));

        return systemMessage;

    }
}