package com.theatre.bookingservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class PerformanceBookedSeatsResponse extends CommonResponse {
    private UUID performanceId;
    // Reference seat ids (seat_db seat.seat_id) that are booked.
    private List<UUID> bookedSeatIds;
    // Human-readable seat refs, aligned for convenience.
    private List<String> bookedSeatRefs;
}
