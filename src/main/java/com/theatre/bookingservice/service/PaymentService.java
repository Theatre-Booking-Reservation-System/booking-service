package com.theatre.bookingservice.service;

import com.theatre.bookingservice.exception.ServiceException;
import com.theatre.bookingservice.model.PaymentDetails;
import com.theatre.bookingservice.util.ErrorCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResult authorise(PaymentDetails details, BigDecimal amount) {
        if (details == null) {
            throw new ServiceException(ErrorCode.PAYMENT_FAILED);
        }

        String pan = digitsOnly(details.getCardNumber());
        String cvv = digitsOnly(details.getCvv());

        boolean validPan = pan.length() >= 13 && pan.length() <= 19;
        boolean validCvv = cvv.length() == 3 || cvv.length() == 4;
        boolean validExpiry = details.getExpiry() != null && details.getExpiry().matches("\\d{2}/\\d{2,4}");
        boolean validAmount = amount != null && amount.signum() > 0;

        if (!validPan || !validCvv || !validExpiry || !validAmount) {
            throw new ServiceException(ErrorCode.PAYMENT_FAILED);
        }

        String last4 = pan.substring(pan.length() - 4);
        String token = "SIMPAY-" + UUID.randomUUID();
        return new PaymentResult(token, last4);
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public record PaymentResult(String paymentToken, String cardLast4) {
    }
}
