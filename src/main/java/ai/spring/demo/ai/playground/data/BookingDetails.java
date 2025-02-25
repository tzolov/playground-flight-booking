package ai.spring.demo.ai.playground.data;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL) // @formatter:off
public record BookingDetails(String bookingNumber,
        String firstName,
        String lastName,
        LocalDate date,
        BookingStatus bookingStatus,
        String from,
        String to,
        String bookingClass) {
} // @formatter:on