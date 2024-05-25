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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

/**
 * * @author Christian Tzolov
 */
@Service
public class CustomerSupportAssistant {

	private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAssistant.class);

	private static String CONVERSATION_ID = "default";

	private final ChatClient chatClient;

	public CustomerSupportAssistant(ChatModel chatModel, VectorStore vectorStore, ChatMemory chatMemory) {

		// @formatter:off
		this.chatClient = ChatClient.builder(chatModel)
				.defaultSystem("""

						You are a customer chat support agent of an airline named "Funnair".",
						Respond in a friendly, helpful, and joyful manner.
						Before providing information about a booking or cancelling a booking,
						you MUST always get the following information from the user:
						booking number, customer first name and last name.
						Before changing a booking you MUST ensure it is permitted by the terms.
						If there is a charge for the change, you MUST ask the user to consent before proceeding.

						""")

				.defaultAdvisor(
						new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()),
						new MessageChatMemoryAdvisor(CONVERSATION_ID, chatMemory))
						// new PromptChatMemoryAdvisor(CONVERSATION_ID, chatMemory))

				.defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking")

				.build();
		// @formatter:on
	}

	public Flux<String> chat(String chatId, String userMessageContent) {

		return this.chatClient.prompt()
				.user(userMessageContent)
				.stream().content();
	}
}