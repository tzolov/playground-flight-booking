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
package ai.spring.demo.ai.playground.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks;

/**
 * @author Christian Tzolov
 * @since 1.0.0
 */
@Service
public class SeatChangeQueue {

    public record SeatChangeRequest(String requestId) {
	}

	private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, Sinks.Many<SeatChangeRequest>> seatChangeRequests = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, CompletableFuture<String>> getPendingRequests() {
        return pendingRequests;
    }

    public ConcurrentHashMap<String, Sinks.Many<SeatChangeRequest>> getSeatChangeRequests() {
        return seatChangeRequests;
    }

}
