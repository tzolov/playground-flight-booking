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

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * * @author Christian Tzolov
 */
@Service
public class CustomerSupportAssistant {

	private final ChatClient chatClient;
	
	// @formatter:off
	public CustomerSupportAssistant(ChatClient.Builder chatClientBuilder, 
		VectorStore vectorStore, ChatMemory chatMemory) {

		
		this.chatClient = chatClientBuilder
				.defaultSystem("""
						You are a customer chat support agent of an airline named "Funnair"."
						Respond in a friendly, helpful, and joyful manner.
						You are interacting with customers through an online chat system.						
					
						Before answering a question about a booking or cancelling a booking, you MUST always
						get the following information from the user: booking number, customer first name and last name.

						If you can not retrieve the status of my flight, please just say "I am sorry, I can not find the booking details".

						Check the message history for booking details before asking the user.

						Before changing a booking you MUST ensure it is permitted by the terms.
						If there is a charge for the change, you MUST ask the user to consent before proceeding.
						Use the provided functions to fetch booking details, change bookings, and cancel bookings.		
						
						
					""")	
				.defaultAdvisors(
						new PromptChatMemoryAdvisor(chatMemory), // Conversation Memory
						new QuestionAnswerAdvisor(vectorStore) // RAG
				)						
				.defaultFunctions("getBookingDetails", "changeBooking", "cancelBooking", "changeSeat") // FUNCTION CALLING
				.build();
	}

	public Flux<String> chat(String chatId, String userMessage) {

		return this.chatClient.prompt()
			.user(userMessage)
			.toolContext(Map.of("chat_id", chatId))
			.advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
				.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
			.stream()
			.content();	
	}
	// @formatter:on

}