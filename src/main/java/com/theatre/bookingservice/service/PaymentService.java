package com.theatre.bookingservice.service;

import com.theatre.bookingservice.exception.ServiceException;
import com.theatre.bookingservice.model.PaymentDetails;
import com.theatre.bookingservice.util.ErrorCode;
import com.theatre.bookingservice.util.PaymentMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    public PaymentResult authorise(PaymentMethod method, PaymentDetails details, BigDecimal amount) {
        if (method == null || amount == null || amount.signum() <= 0) {
            throw new ServiceException(ErrorCode.PAYMENT_FAILED);
        }

        return switch (method) {
            case CREDIT_CARD, DEBIT_CARD -> authoriseCard(details);
            case EWALLET, BANK_TRANSFER -> authoriseCardless();
        };
    }

    private PaymentResult authoriseCard(PaymentDetails details) {
        if (details == null) {
            throw new ServiceException(ErrorCode.PAYMENT_FAILED);
        }

        String pan = digitsOnly(details.getCardNumber());
        String cvv = digitsOnly(details.getCvv());

        boolean validPan = pan.length() >= 13 && pan.length() <= 19;
        boolean validCvv = cvv.length() == 3 || cvv.length() == 4;
        boolean validExpiry = details.getExpiry() != null && details.getExpiry().matches("\\d{2}/\\d{2,4}");

        if (!validPan || !validCvv || !validExpiry) {
            throw new ServiceException(ErrorCode.PAYMENT_FAILED);
        }

        String last4 = pan.substring(pan.length() - 4);
        return new PaymentResult(newToken(), last4);
    }

    private PaymentResult authoriseCardless() {
        // E-wallet / bank transfer: no card data to validate or retain.
        return new PaymentResult(newToken(), null);
    }

    private String newToken() {
        return "SIMPAY-" + UUID.randomUUID();
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public record PaymentResult(String paymentToken, String cardLast4) {
    }
}
