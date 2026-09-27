package com.theatre.bookingservice.repository.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {
    // Soft reference to seat_db seat.seat_id (the reference seat table).
    private UUID seatId;
    // Human-readable label e.g. "STALLS AA1".
    private String seatRef;
    private String zoneName;
    private String section;
}
