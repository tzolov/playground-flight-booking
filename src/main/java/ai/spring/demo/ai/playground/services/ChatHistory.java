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
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

/**
 * Simple, in memory, message chat history.
 *
 * @author Christian Tzolov
 */
@Service
public class ChatHistory {

	private static final Logger logger = LoggerFactory.getLogger(ChatHistory.class);

	private final Map<String, List<Message>> chatHistoryLog;

	/**
	 * Temporal storage used to aggregate streaming messages until the finishReason=STOP is received.
	 */
	private final Map<String, List<Message>> messageAggregations;

	public ChatHistory() {
		this.chatHistoryLog = new ConcurrentHashMap<>();
		this.messageAggregations = new ConcurrentHashMap<>();
	}

	/**
	 * Messages are grouped by chatId and messageId. The streaming messages are aggregated until the finishReason=STOP
	 * is received. Then the aggregations are collapsed in to a single assistant message. The user messages are
	 * committed to the history as is.
	 *
	 * @param chatId the chat id
	 * @param message the message to add
	 */
	public void addMessage(String chatId, Message message) {

		String groupId = toGroupId(chatId, message);

		this.messageAggregations.putIfAbsent(groupId, new ArrayList<>());

		if (this.messageAggregations.keySet().size() > 1) {
			logger.warn("Multiple active sessions: " + this.messageAggregations.keySet());
		}

		this.messageAggregations.get(groupId).add(message);

		String finish = getProperty(message, "finishReason");
		if (finish.equalsIgnoreCase("STOP") || message.getMessageType() == MessageType.USER) {
			this.finalizeMessageGroup(chatId, groupId);
		}
	}

	private String toGroupId(String chatId, Message message) {
		String messageId = getProperty(message, "id");
		return chatId + ":" + messageId;
	}

	private String getProperty(Message message, String key) {
		Map<String, Object> properties = message.getProperties();
		if (properties != null && properties.containsKey(key)) {
			return (String) properties.get(key);
		}
		return "";
	}

	private void finalizeMessageGroup(String chatId, String groupId) {
		if (this.messageAggregations.containsKey(groupId)) {
			
			List<Message> sessionMessages = this.messageAggregations.get(groupId);
			if (sessionMessages.size() == 1) {
				this.commitToHistoryLog(chatId, sessionMessages.get(0));
			}
			else {
				String aggregatedContent = sessionMessages.stream()
						.filter(m -> m.getContent() != null)
						.map(m -> m.getContent()).collect(Collectors.joining());
				this.commitToHistoryLog(chatId, new AssistantMessage(aggregatedContent));
			}
			this.messageAggregations.remove(groupId);
		}
		else {
			logger.warn("No active session for groupId: " + groupId);
		}
	}

	private void commitToHistoryLog(String chatId, Message message) {
		this.chatHistoryLog.putIfAbsent(chatId, new ArrayList<>());
		this.chatHistoryLog.get(chatId).add(message);
	}

	public List<Message> getAll(String chatId) {
		if (!this.chatHistoryLog.containsKey(chatId)) {
			return List.of();
		}
		return this.chatHistoryLog.get(chatId);
	}

	public List<Message> getLastN(String chatId, int lastN) {
		if (!this.chatHistoryLog.containsKey(chatId)) {
			return List.of();
		}
		List<Message> response = this.chatHistoryLog.get(chatId);
		if (this.chatHistoryLog.get(chatId).size() < lastN) {
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
