package ai.spring.demo.ai.playground.services;

import java.util.concurrent.CompletableFuture;

import ai.spring.demo.ai.playground.data.BookingDetails;
import ai.spring.demo.ai.playground.services.SeatChangeQueue.SeatChangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

@Service
public class BookingTools {

	private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

	private final FlightBookingService flightBookingService;

	private final SeatChangeQueue shared;

	@Autowired
	public BookingTools(FlightBookingService flightBookingService, SeatChangeQueue shared) {
		this.flightBookingService = flightBookingService;
		this.shared = shared;
	}

	@Tool(description = "Get booking details")
	public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName,
			ToolContext toolContext) {
		try {
			return flightBookingService.getBookingDetails(bookingNumber, firstName, lastName);
		} catch (Exception e) {
			logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
			return new BookingDetails(bookingNumber, firstName, lastName, null, null,
					null, null, null, null);
		}
	}

	@Tool(description = "Change booking dates")
	public void changeBooking(String bookingNumber, String firstName, String lastName, String newDate, String from,
			String to, ToolContext toolContext) {
		flightBookingService.changeBooking(bookingNumber, firstName, lastName, newDate, from, to);
	};

	@Tool(description = "Cancel booking")
	public void cancelBooking(String bookingNumber, String firstName, String lastName, ToolContext toolContext) {
		flightBookingService.cancelBooking(bookingNumber, firstName, lastName);
	}

	@Tool(description = "Change seat")
	public void changeSeat(String bookingNumber, String firstName, String lastName, ToolContext toolContext) {

		System.out.println("Changing seat for " + bookingNumber + " to a better one");

		var chatId = toolContext.getContext().get("chat_id").toString();

		CompletableFuture<String> future = new CompletableFuture<>();
		shared.getPendingRequests().put(chatId, future);

		shared.getSeatChangeRequests().values().forEach(sink -> sink.tryEmitNext(new SeatChangeRequest(chatId)));

		// Wait for the seat selection to complete
		String seat;
		try {
			// This will block until completeSeatChangeRequest is called
			seat = future.get();
		} catch (Exception e) {
			throw new RuntimeException("Seat selection interrupted", e);
		}

		// Proceed with changing the seat
		flightBookingService.changeSeat(bookingNumber, firstName, lastName, seat);
	}

}
