package com.theatre.bookingservice.model;

import lombok.Data;

import java.util.UUID;

@Data
public class SeatSelection {
    private UUID seatId;
    private String seatRef;
    private String zoneName;
    private String section;
}
