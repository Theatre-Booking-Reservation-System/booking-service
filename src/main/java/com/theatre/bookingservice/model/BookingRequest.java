package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.PaymentMethod;
import com.theatre.bookingservice.util.TicketType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {
    // The registered patron placing the booking (soft ref to identity_db).
    private UUID patronId;
    private UUID performanceId;
    private List<SeatSelection> seats;
    private TicketType ticketType;
    private PaymentMethod paymentMethod;
    private PaymentDetails paymentDetails;
}
