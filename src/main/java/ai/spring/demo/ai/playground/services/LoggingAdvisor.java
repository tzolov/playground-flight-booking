package ai.spring.demo.ai.playground.services;

import java.util.Map;

import org.springframework.ai.chat.client.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.RequestAdvisor;

public class LoggingAdvisor implements RequestAdvisor {

	@Override
	public AdvisedRequest adviseRequest(AdvisedRequest request, Map<String, Object> context) {
		System.out.println("Request: " + request);
		return request;
	}

	@Override
	public String getName() {
		return "LoggingAdvisor";
	}
}