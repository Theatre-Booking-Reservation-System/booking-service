package com.theatre.bookingservice.model;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {
    // Patron placing the booking; NULL for a guest booking.
    private UUID patronId;
    // Required when patronId is NULL.
    private String guestEmail;
    private UUID performanceId;
    private String paymentToken;
    private List<BookingLineRequest> lines;
}
