package ai.spring.demo.ai.playground.client;



import ai.spring.demo.ai.playground.services.CustomerSupportAgent;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;
import reactor.core.publisher.Flux;

@BrowserCallable
@AnonymousAllowed
public class AssistantService {

    private final CustomerSupportAgent agent;


    public AssistantService(CustomerSupportAgent agent) {
        this.agent = agent;
    }

    public Flux<String> chat(String chatId, String userMessage) {
        return agent.chat(chatId, userMessage);
    }
}
