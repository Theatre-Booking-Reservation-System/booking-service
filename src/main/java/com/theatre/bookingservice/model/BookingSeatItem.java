package com.theatre.bookingservice.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BookingSeatItem {
    private UUID seatId;
    private String seatRef;
    private String zoneName;
    private String section;
}
