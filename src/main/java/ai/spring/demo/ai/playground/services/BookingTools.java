package ai.spring.demo.ai.playground.services;

import java.time.LocalDate;
import java.util.function.Function;

import ai.spring.demo.ai.playground.data.BookingStatus;
import groovyjarjarpicocli.CommandLine.ExecutionException;
import reactor.core.publisher.Sinks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class BookingTools {

	private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

	@Autowired
	private FlightBookingService flightBookingService;

	public record BookingDetailsRequest(String bookingNumber, String firstName, String lastName) {
	}

	public record ChangeBookingDatesRequest(String bookingNumber, String firstName, String lastName, String date,
			String from, String to) {
	}

	public record CancelBookingRequest(String bookingNumber, String firstName, String lastName) {
	}

	// @ formatter:off
	@JsonInclude(Include.NON_NULL)
	public record BookingDetails(String bookingNumber, String firstName, String lastName, LocalDate date,
			BookingStatus bookingStatus, String from, String to, String seatNumber, String bookingClass) {
	}
	// @ formatter:on

	@Bean
	@Description("Get booking details")
	public Function<BookingDetailsRequest, BookingDetails> getBookingDetails() {
		return request -> {
			try {
				return flightBookingService.getBookingDetails(request.bookingNumber(), request.firstName(),
						request.lastName());
			}
			catch (Exception e) {
				logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
				return new BookingDetails(request.bookingNumber(), request.firstName(), request.lastName, null, null,
						null, null, null, null);
			}
		};
	}

	@Bean
	@Description("Change booking dates")
	public Function<ChangeBookingDatesRequest, String> changeBooking() {
		return request -> {
			flightBookingService.changeBooking(request.bookingNumber(), request.firstName(), request.lastName(),
					request.date(), request.from(), request.to());
			return "";
		};
	}

	@Bean
	@Description("Cancel booking")
	public Function<CancelBookingRequest, String> cancelBooking() {
		return request -> {
			flightBookingService.cancelBooking(request.bookingNumber(), request.firstName(), request.lastName());
			return "";
		};
	}

	public record SeatChangeRequest(String requestId) {
	}

	public record LLMSeatChangeRequest(String bookingNumber, String firstName, String lastName) {
	}

	private final ConcurrentHashMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<String, Sinks.Many<SeatChangeRequest>> seatChangeRequests = new ConcurrentHashMap<>();

	@Bean
	@Description("Change seat")
	public Function<LLMSeatChangeRequest, String> changeSeat() {
		return request -> {
			System.out.println("Changing seat for " + request.bookingNumber() + " to a better one");

			var id = UUID.randomUUID().toString();
			CompletableFuture<String> future = new CompletableFuture<>();
			pendingRequests.put(id, future);

			// FIXME: Only send the request to the correct chatId
			// This function is called from the LLM, that does not know the internal
			// chatId, how do we get it?
			// Advisor context maybe?
			seatChangeRequests.values().forEach(sink -> sink.tryEmitNext(new SeatChangeRequest(id)));

			// Wait for the seat selection to complete
			String seat;
			try {
				seat = future.get(); // This will block until completeSeatChangeRequest is
										// called
			}
			catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException("Seat selection interrupted", e);
			}
			catch (java.util.concurrent.ExecutionException e) {
				throw new RuntimeException("Seat selection interrupted", e);
			}

			// Proceed with changing the seat
			flightBookingService.changeSeat(request.bookingNumber(), request.firstName(), request.lastName(), seat);

			return "Seat changed successfully";
		};
	}

}
