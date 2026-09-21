package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class BookingResponse extends CommonResponse {
    private UUID bookingId;
    private String bookingRef;
    private UUID patronId;
    private String guestEmail;
    private UUID performanceId;
    private BookingStatus status;
    private Boolean isFlagged;
    private BigDecimal subtotalLkr;
    private BigDecimal discountLkr;
    private BigDecimal vatLkr;
    private BigDecimal totalLkr;
    private String paymentToken;
    private PaymentStatus paymentStatus;
    private OffsetDateTime createdAt;
    private List<BookingLineItem> lines;
}
