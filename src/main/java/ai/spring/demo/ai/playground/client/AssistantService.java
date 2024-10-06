package ai.spring.demo.ai.playground.client;

import ai.spring.demo.ai.playground.services.CustomerSupportAssistant;
import ai.spring.demo.ai.playground.services.SeatChangeQueue;
import ai.spring.demo.ai.playground.services.SeatChangeQueue.SeatChangeRequest;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@BrowserCallable
@AnonymousAllowed
public class AssistantService {

	private final CustomerSupportAssistant agent;

	private SeatChangeQueue seatChangeQueue;

	public AssistantService(CustomerSupportAssistant agent, SeatChangeQueue shared) {
		this.agent = agent;
		this.seatChangeQueue = shared;
	}

	public Flux<String> chat(String chatId, String userMessage) {
		return this.agent.chat(chatId, userMessage);
	}

	public Flux<SeatChangeRequest> seatChangeRequests(String chatId) {
		return this.seatChangeQueue.getSeatChangeRequests()
			.computeIfAbsent(chatId, id -> Sinks.many().unicast().onBackpressureBuffer())
			.asFlux();
	}

	public void completeSeatChangeRequest(String requestId, String seat) {
		var future = this.seatChangeQueue.getPendingRequests().remove(requestId);
		if (future != null) {
			future.complete(seat);
		}
	}

}
