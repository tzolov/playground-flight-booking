package ai.spring.demo.ai.playground.services;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ai.spring.demo.ai.playground.data.BookingStatus;
import ai.spring.demo.ai.playground.services.SeatChangeQueue.SeatChangeRequest;

@Configuration
public class BookingTools {

	private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

	@Autowired
	private FlightBookingService flightBookingService;

	@Autowired
	private SeatChangeQueue shared;

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

	public record LLMSeatChangeRequest(String bookingNumber, String firstName, String lastName) {
	}

	@Bean
	@Description("Change flight booking seat")
	public BiFunction<LLMSeatChangeRequest, ToolContext, String> changeSeat() {
		return (request, toolContext) -> {
			System.out.println("Changing seat for " + request.bookingNumber() + " to a better one");

			var chatId = toolContext.getContext().get("chat_id").toString();

			CompletableFuture<String> future = new CompletableFuture<>();
			shared.getPendingRequests().put(chatId, future);

			shared.getSeatChangeRequests().values().forEach(sink -> sink.tryEmitNext(new SeatChangeRequest(chatId)));

			// Wait for the seat selection to complete
			String seat;
			try {
				// This will block until completeSeatChangeRequest is called
				seat = future.get();
			}
			catch (Exception e) {
				throw new RuntimeException("Seat selection interrupted", e);
			}

			// Proceed with changing the seat
			flightBookingService.changeSeat(request.bookingNumber(), request.firstName(), request.lastName(), seat);

			return "Seat changed successfully";
		};
	}

}
