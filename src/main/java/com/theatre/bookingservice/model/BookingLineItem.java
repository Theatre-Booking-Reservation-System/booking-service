package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.ConcessionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BookingLineItem {
    private UUID lineId;
    private UUID perfSeatId;
    private String seatRef;
    private String zoneName;
    private String sessionType;
    private ConcessionType concessionType;
    private BigDecimal basePriceLkr;
    private BigDecimal concessionDiscLkr;
    private BigDecimal loyaltyDiscLkr;
    private BigDecimal vatLkr;
    private BigDecimal finalPriceLkr;
}
