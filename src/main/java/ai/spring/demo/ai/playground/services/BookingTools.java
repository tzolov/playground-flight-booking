package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.BookingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.stereotype.Service;

@Service 
public class BookingTools {

    private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

    private final FlightBookingService flightBookingService;

    @Autowired
    public BookingTools(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }

    @Tool(description = "Get booking details")
    public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName,
            ToolContext toolContext) {
        try {
            return flightBookingService.getBookingDetails(bookingNumber, firstName, lastName);
        } catch (Exception e) {
            logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            return new BookingDetails(bookingNumber, firstName, lastName, null, null,
                    null, null, null);
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
}
