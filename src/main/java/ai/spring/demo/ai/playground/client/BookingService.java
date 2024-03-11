package ai.spring.demo.ai.playground.client;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.hilla.BrowserCallable;

import java.util.List;

import ai.spring.demo.ai.playground.services.BookingDetails;
import ai.spring.demo.ai.playground.services.FlightService;

@BrowserCallable
@AnonymousAllowed
public class BookingService {
    private final FlightService carRentalService;

    public BookingService(FlightService carRentalService) {
        this.carRentalService = carRentalService;
    }

    public List<BookingDetails> getBookings() {
        return carRentalService.getBookings();
    }
}
