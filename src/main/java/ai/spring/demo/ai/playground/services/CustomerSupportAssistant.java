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

import java.time.LocalDate;

import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;


/**
 * * @author Christian Tzolov
 */
@Service
public class CustomerSupportAssistant {

	private final ChatClient chatClient;

	public CustomerSupportAssistant(ChatClient.Builder modelBuilder, VectorStore vectorStore, ChatMemory chatMemory, BookingTools bookingTools) {

		// @formatter:off
		this.chatClient = modelBuilder
				.defaultSystem("""
						You are a customer chat support agent of an airline named "Funnair"."
						Respond in a friendly, helpful, and joyful manner.
						You are interacting with customers through an online chat system.
						Before providing information about a booking or cancelling a booking, you MUST always
						get the following information from the user: booking number, customer first name and last name.
						Check the message history for this information before asking the user.
						Before changing a booking you MUST ensure it is permitted by the terms.
						If there is a charge for the change, you MUST ask the user to consent before proceeding.
						Use the provided functions to fetch booking details, change bookings, and cancel bookings.
						Use parallel function calling if required.
						Today is {current_date}.
					""")
				.defaultAdvisors(
						PromptChatMemoryAdvisor.builder(chatMemory)
								.build(),
						// new VectorStoreChatMemoryAdvisor(vectorStore)),
						QuestionAnswerAdvisor.builder(vectorStore)
								.searchRequest(SearchRequest.builder().build())
								.build(), // RAG
						// new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()
						// 	.withFilterExpression("'documentType' == 'terms-of-service' && region in ['EU', 'US']")),
						// RetrievalAugmentationAdvisor.builder()
						// 	.documentRetriever(VectorStoreDocumentRetriever.builder().vectorStore(vectorStore).build())
						// 	.queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
						// 	.build() // RAG
						
						new SimpleLoggerAdvisor())
						
				.defaultTools(bookingTools) // FUNCTION CALLING

				.build();
		// @formatter:on
	}

	public Flux<String> chat(String chatId, String userMessageContent) {

		return this.chatClient.prompt()
				.system(s -> s.param("current_date", LocalDate.now().toString()))
				.user(userMessageContent)
				.advisors(a -> a
						.param(ChatMemory.CONVERSATION_ID, chatId)
						//.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
						)
				.stream().content();
	}
}