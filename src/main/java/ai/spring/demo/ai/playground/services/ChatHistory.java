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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

/**
 * Simple, in memory, message chat history store.
 *
 * @author Christian Tzolov
 */
@Service
public class ChatHistory {

	private static final Logger logger = LoggerFactory.getLogger(ChatHistory.class);

	private final Map<String, List<Message>> history;

	private final Map<String, List<Message>> messageAggregations;

	public ChatHistory() {
		this.history = new ConcurrentHashMap<>();
		this.messageAggregations = new ConcurrentHashMap<>();
	}

	public void addMessage(String chatId, Message message) {
		this.history.putIfAbsent(chatId, new ArrayList<>());
		this.history.get(chatId).add(message);
	}

	private String chunkGroupId(String chatId, String messageId) {
		return chatId + ":" + messageId;
	}

	/**
	 * Aggregates all chunk messages into a single message and adds it to the chat history. Aggregation completes on
	 * finishReason=STOP.
	 * @param chatId the chat id
	 * @param message the message chunk
	 */
	public void addMessageChunk(String chatId, Message message) {

		String id = (message.getProperties() != null) ? (String) message.getProperties().get("id") : "";
		String chunkGroupId = chunkGroupId(chatId, id);

		this.messageAggregations.putIfAbsent(chunkGroupId, new ArrayList<>());
		if (this.messageAggregations.keySet().size() > 1) {
			logger.warn("Multiple active sessions: " + this.messageAggregations.keySet());
		}
		this.messageAggregations.get(chunkGroupId).add(message);

		String finish = (message.getProperties() != null) ? (String) message.getProperties().get("finishReason") : "";
		if (finish.equalsIgnoreCase("STOP")) {
			this.finalizeMessageGroup(chatId, chunkGroupId);
		}
	}

	private void finalizeMessageGroup(String chatId, String groupId) {
		if (this.messageAggregations.containsKey(groupId)) {
			List<Message> sessionMessages = this.messageAggregations.get(groupId);
			String aggregatedContent = sessionMessages.stream()
					.filter(m -> m.getContent() != null)
					.map(m -> m.getContent()).collect(Collectors.joining());
			this.addMessage(chatId, new AssistantMessage(aggregatedContent));
			this.messageAggregations.remove(groupId);
		}
		else {
			logger.warn("No active session for groupId: " + groupId);
		}
	}

	public List<Message> getAll(String chatId) {
		if (!this.history.containsKey(chatId)) {
			return List.of();
		}
		return this.history.get(chatId);
	}

	public List<Message> getLastN(String chatId, int lastN) {
		if (!this.history.containsKey(chatId)) {
			return List.of();
		}
		List<Message> response = this.history.get(chatId);
		if (this.history.get(chatId).size() < lastN) {
			return response;
		}

		int from = response.size() - lastN;
		int to = response.size();
		logger.info("Returning last {} messages from {} to {}", lastN, from, to);

		var responseWindow = response.subList(from, to);
		logger.info("Returning last {} messages: {}", lastN, responseWindow);

		return responseWindow;
	}
}
