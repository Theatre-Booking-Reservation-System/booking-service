package com.theatre.bookingservice.model;

import lombok.Data;

@Data
public class PaymentDetails {
    private String cardNumber;
    private String expiry;
    private String cvv;
    private String cardHolderName;
}
