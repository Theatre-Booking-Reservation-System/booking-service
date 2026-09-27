package com.theatre.bookingservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class RecentBookingsResponse extends CommonResponse {
    private List<RecentBookingItem> bookings;
}
