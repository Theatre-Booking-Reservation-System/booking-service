package com.theatre.bookingservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class BookingListResponse extends CommonResponse {
    private List<BookingItem> bookings;
}
