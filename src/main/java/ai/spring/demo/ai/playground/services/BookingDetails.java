package ai.spring.demo.ai.playground.services;

import java.time.LocalDate;

import ai.spring.demo.ai.playground.data.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record BookingDetails(String bookingNumber,
        String firstName,
        String lastName,
        LocalDate date,
        BookingStatus bookingStatus,
        String from,
        String to,
        String bookingClass) {
}
