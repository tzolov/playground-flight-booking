package ai.spring.demo.ai.playground.services;

import java.time.LocalDate;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.NestedExceptionUtils;

@Configuration
public class BookingTools {

    private static final Logger logger = LoggerFactory.getLogger(BookingTools.class);

    @Autowired
    private FlightService carRentalService;

    public record BookingDetailsRequest(String bookingNumber, String firstName, String lastName) {
    }

    public record ChangeBookingDatesRequest(String bookingNumber, String firstName, String lastName, String date,
            String from, String to) {
    }

    public record CancelBookingRequest(String bookingNumber, String firstName, String lastName) {
    }

    @Bean
    @Description("Get booking details")
    public Function<BookingDetailsRequest, BookingDetails> getBookingDetails() {
        return request -> {
            try {
                return carRentalService.getBookingDetails(request.bookingNumber(), request.firstName(),
                        request.lastName());
            }
            catch (Exception e) {
                logger.warn("Booking details: {}", NestedExceptionUtils.getMostSpecificCause(e).getMessage());
                return new BookingDetails(request.bookingNumber(), request.firstName(), request.lastName,
                        null, null, null, null, null);
            }
        };
    }

    @Bean
    @Description("Change booking dates")
    public Function<ChangeBookingDatesRequest, String> changeBooking() {
        return request -> {
            carRentalService.changeBooking(request.bookingNumber(), request.firstName(), request.lastName(),
                    request.date(), request.from(), request.to());
            return "";
        };
    }

    @Bean
    @Description("Cancel booking")
    public Function<CancelBookingRequest, String> cancelBooking() {
        return request -> {
            carRentalService.cancelBooking(request.bookingNumber(), request.firstName(), request.lastName());
            return "";
        };
    }
}
