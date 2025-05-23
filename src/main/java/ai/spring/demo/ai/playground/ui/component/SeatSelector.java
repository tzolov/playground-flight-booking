package ai.spring.demo.ai.playground.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;

public class SeatSelector extends Composite<VerticalLayout> {
    private static final int ROWS = 12;
    private static final int SEATS_PER_ROW = 6;
    private static final char[] SEAT_LETTERS = {'A', 'B', 'C', 'D', 'E', 'F'};

    private String selectedSeat;
    private final VerticalLayout seatGrid = new VerticalLayout();

    /**
     * Creates a new SeatSelector component.
     */
    public SeatSelector() {
        initializeUI();
    }

    /**
     * Creates a new SeatSelector component with a pre-selected seat.
     * 
     * @param selectedSeat the initially selected seat (e.g. "1A")
     */
    public SeatSelector(String selectedSeat) {
        this.selectedSeat = selectedSeat;
        initializeUI();
    }

    private void initializeUI() {
        seatGrid.setPadding(false);
        seatGrid.setSpacing(false);

        renderSeats();

        getContent().add(seatGrid);
        getContent().setPadding(false);
        getContent().setSpacing(false);
    }

    /**
     * Renders the seat grid.
     */
    private void renderSeats() {
        seatGrid.removeAll();

        for (int row = 1; row <= ROWS; row++) {
            FlexLayout seatRow = new FlexLayout();
            seatRow.setAlignItems(FlexLayout.Alignment.CENTER);

            // Add row number
            Span rowNumber = new Span(String.valueOf(row));
            rowNumber.getStyle().set("width", "20px");
            seatRow.add(rowNumber);

            for (int seatIndex = 0; seatIndex < SEATS_PER_ROW; seatIndex++) {
                char seatLetter = SEAT_LETTERS[seatIndex];
                String seatId = row + String.valueOf(seatLetter);
                boolean isSelected = seatId.equals(selectedSeat);

                Button seat = createSeatButton(seatId, seatLetter, isSelected);
                seatRow.add(seat);

                // Insert aisle after seat C
                if (seatLetter == 'C') {
                    Div aisle = new Div();
                    aisle.getStyle().set("width", "20px");
                    seatRow.add(aisle);
                }
            }

            seatGrid.add(seatRow);
        }
    }

    /**
     * Creates a button representing a seat.
     * 
     * @param seatId the seat identifier (e.g. "1A")
     * @param seatLetter the seat letter
     * @param isSelected whether the seat is currently selected
     * @return the seat button
     */
    private Button createSeatButton(String seatId, char seatLetter, boolean isSelected) {
        Button seat = new Button(String.valueOf(seatLetter));
        seat.getStyle()
            .set("width", "30px")
            .set("height", "30px")
            .set("margin", "2px")
            .set("padding", "0")
            .set("background-color", isSelected ? "blue" : "lightgray")
            .set("color", isSelected ? "white" : "black")
            .set("text-align", "center")
            .set("line-height", "30px")
            .set("cursor", "pointer");

        seat.addClickListener(event -> handleSeatClick(seatId));

        return seat;
    }

    /**
     * Handles a seat click event.
     * 
     * @param seatId the clicked seat identifier
     */
    private void handleSeatClick(String seatId) {
        selectedSeat = seatId;
        renderSeats(); // Re-render to update the selected seat
        fireEvent(new SeatSelectedEvent(this, seatId));
    }

    /**
     * Gets the currently selected seat.
     * 
     * @return the selected seat identifier
     */
    public String getSelectedSeat() {
        return selectedSeat;
    }

    /**
     * Sets the selected seat.
     * 
     * @param selectedSeat the seat identifier to select
     */
    public void setSelectedSeat(String selectedSeat) {
        this.selectedSeat = selectedSeat;
        renderSeats(); // Re-render to update the selected seat
    }

    /**
     * Adds a listener for seat selection events.
     * 
     * @param listener the listener to add
     * @return a registration for removing the listener
     */
    public Registration addSeatSelectedListener(ComponentEventListener<SeatSelectedEvent> listener) {
        return addListener(SeatSelectedEvent.class, listener);
    }

    /**
     * Event fired when a seat is selected.
     */
    public static class SeatSelectedEvent extends ComponentEvent<Component> {
        private final String seatId;

        /**
         * Creates a new seat selected event.
         * 
         * @param source the source component
         * @param seatId the selected seat identifier
         */
        public SeatSelectedEvent(Component source, String seatId) {
            super(source, false);
            this.seatId = seatId;
        }

        /**
         * Gets the selected seat identifier.
         * 
         * @return the selected seat identifier
         */
        public String getSeatId() {
            return seatId;
        }
    }
}
