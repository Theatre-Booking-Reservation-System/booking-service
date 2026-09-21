package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.ConcessionType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BookingLineRequest {
    private UUID perfSeatId;
    private String seatRef;
    private String zoneName;
    private String sessionType;
    private ConcessionType concessionType;
    // Raw NIC/passport, encrypted before persistence. Only present with a concession.
    private String nicPassport;
    private BigDecimal basePriceLkr;
    private BigDecimal concessionDiscLkr;
    private BigDecimal loyaltyDiscLkr;
    private BigDecimal vatLkr;
    private BigDecimal finalPriceLkr;
}
