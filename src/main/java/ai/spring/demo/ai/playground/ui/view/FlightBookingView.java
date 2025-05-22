package ai.spring.demo.ai.playground.ui.view;

import ai.spring.demo.ai.playground.data.BookingDetails;
import ai.spring.demo.ai.playground.services.CustomerSupportAssistant;
import ai.spring.demo.ai.playground.services.FlightBookingService;
import ai.spring.demo.ai.playground.ui.component.SeatSelector;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.Route;
import org.springframework.ai.tool.annotation.Tool;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Route("")
public class FlightBookingView extends SplitLayout {


    private final FlightBookingService flightBookingService;
    private final CustomerSupportAssistant assistant;
    private Grid<BookingDetails> grid;
    private final String chatId = UUID.randomUUID().toString();
    private UI ui;

    public FlightBookingView(
        FlightBookingService flightBookingService,
        CustomerSupportAssistant assistant
    ) {
        this.flightBookingService = flightBookingService;
        this.assistant = assistant;
        setSizeFull();
        setOrientation(Orientation.HORIZONTAL);
        setSplitterPosition(30);

        addToPrimary(createChatLayout());
        addToSecondary(createGrid());

        updateBookings();
    }


    private Component createChatLayout() {
        var chatLayout = new VerticalLayout();
        var messageList = new MessageList();
        var messageInput = new MessageInput();

        messageList.setMarkdown(true);
        chatLayout.setPadding(false);

        messageInput.setWidthFull();
        messageInput.addSubmitListener(e -> handleMessageInput(e.getValue(), messageList));

        chatLayout.addAndExpand(messageList);
        chatLayout.add(messageInput);
        return chatLayout;
    }

    private Component createGrid() {
        grid = new Grid<>(BookingDetails.class);
        grid.setSizeFull();
        grid.setColumns("bookingNumber", "firstName", "lastName", "date", "bookingStatus", "from", "to", "seatNumber", "bookingClass");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));

        return grid;
    }


    private void handleMessageInput(String userMessage, MessageList messageList) {
        var userMessageItem = new MessageListItem(userMessage, null, "You");
        userMessageItem.setUserColorIndex(1);
        messageList.addItem(userMessageItem);

        var first = new AtomicBoolean(true);
        var responseItem = new MessageListItem("", null, "Assistant");
        responseItem.setUserColorIndex(2);

        assistant.chat(chatId, userMessage, this)
            .doOnComplete(() -> ui.access(this::updateBookings))
            .subscribe(token -> ui.access(() -> {
                if (first.get()) {
                    responseItem.setText(token);
                    messageList.addItem(responseItem);
                    first.set(false);
                } else {
                    responseItem.appendText(token);
                }
            }));
    }

    @Tool(description = "Request the user to select a new seat")
    public String changeSeatNumber(String bookingNumber, String firstName, String lastName) {
        CompletableFuture<String> seatSelectionFuture = new CompletableFuture<>();

        var dialog = new Dialog();
        dialog.setModal(true);
        dialog.setHeaderTitle("Select a new seat for " + firstName + " " + lastName);

        var booking = flightBookingService.getBookingDetails(bookingNumber, firstName, lastName);
        var seatSelector = new SeatSelector(booking.seatNumber());

        seatSelector.addSeatSelectedListener(event -> {
            String newSeatNumber = event.getSeatId();
            flightBookingService.changeSeat(bookingNumber, firstName, lastName, newSeatNumber);
            ui.access(() -> {
                dialog.close();
                updateBookings();
                seatSelectionFuture.complete(newSeatNumber);
            });
        });

        dialog.add(seatSelector);
        ui.access(dialog::open);

        // Wait for the seat selection to complete before returning to ensure the chat flow stays in sync
        try {
            return seatSelectionFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("Error while waiting for seat selection", e);
        }
    }

    private void updateBookings() {
        grid.setItems(flightBookingService.getBookings());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.ui = attachEvent.getUI();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.ui = null;
    }
}
