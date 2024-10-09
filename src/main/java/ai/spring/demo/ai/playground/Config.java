/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ai.spring.demo.ai.playground;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import io.micrometer.observation.ObservationPredicate;

/**
 * @author Christian Tzolov
 * @since 1.0.0
 */
@Configuration
public class Config {
	
	// @formatter:off
	@Bean
	CommandLineRunner ingestTermOfServiceToVectorStore(VectorStore vectorStore,
			@Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {
        
        // Ingest the document into the vector store		
		return args -> vectorStore.write( // 3. WRITE to VectorStore
						new TokenTextSplitter().transform( // 2. CHUNK
								new TextReader(termsOfServiceDocs).read())); // 1. READ
    }
    // @formatter:on

	@Bean
	public ChatMemory chatMemory() {
		return new InMemoryChatMemory();
	}

	// (Optional) Suppress the actuator server observations. 
    // This hides the actuator prometheus traces.
	@Bean
	ObservationPredicate noActuatorServerObservations() {
		return (name, context) -> {
			if (name.equals("http.server.requests")
					&& context instanceof ServerRequestObservationContext serverContext) {
				String requestUri = serverContext.getCarrier().getRequestURI();
				return !requestUri.startsWith("/actuator") && !requestUri.startsWith("/VAADIN")
						&& !requestUri.startsWith("/HILLA") && !requestUri.startsWith("/connect")
						&& !requestUri.startsWith("/**") && !requestUri.equalsIgnoreCase("/");
			}
			else {
				return true;
			}
		};
	}
}
