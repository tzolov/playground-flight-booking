import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;

import reactor.core.publisher.Flux;

public class LoggingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	@Override
	public String getName() {
		return "LoggingAdvisor";
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		System.out.println("\nRequest: " + advisedRequest);
		AdvisedResponse response = chain.nextAroundCall(advisedRequest);
		System.out.println("\nResponse: " + response);
		return response;

	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		System.out.println("\nRequest: " + advisedRequest);
		Flux<AdvisedResponse> responses = chain.nextAroundStream(advisedRequest);
		return new MessageAggregator().aggregateAdvisedResponse(responses, aggregatedAdvisedResponse -> {
			System.out.println("\nResponse: " + aggregatedAdvisedResponse);
		});
	}

}