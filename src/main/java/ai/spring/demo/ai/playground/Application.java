package ai.spring.demo.ai.playground;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

import io.micrometer.observation.ObservationPredicate;

@SpringBootApplication
@Theme(value = "customer-support-agent")
public class Application implements AppShellConfigurator {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}

	// Ingest the document into the vector store
	@Bean
	CommandLineRunner ingestTermOfServiceToVectorStore(VectorStore vectorStore,
			@Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {

		// @formatter:off
		return args -> vectorStore.write(
						new TokenTextSplitter().transform(
								new TextReader(termsOfServiceDocs).read()));
		// @formatter:on
	}

	@Bean
	public ChatMemory chatMemory() {
		return new InMemoryChatMemory();
	}

	// Optional suppress the actuator server observations. This hides the actuator
	// prometheus traces.
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

	// @Bean
	// @ConditionalOnMissingBean
	// public VectorStore vectorStore(EmbeddingModel embeddingModel) {
	// return new SimpleVectorStore(embeddingModel);
	// }

}
