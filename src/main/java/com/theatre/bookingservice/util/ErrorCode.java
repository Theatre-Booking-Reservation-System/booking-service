package com.theatre.bookingservice.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    DEFAULT("ERR_00", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOKING_NOT_FOUND("BKG_01", "Booking Not Found", HttpStatus.NOT_FOUND),
    BOOKING_ALREADY_CANCELLED("BKG_02", "Booking Already Cancelled", HttpStatus.CONFLICT),
    BOOKING_NOT_CANCELLABLE("BKG_03", "Booking Cannot Be Cancelled In Its Current State", HttpStatus.CONFLICT),
    INVALID_BOOKING_REQUEST("BKG_04", "Invalid Booking Request", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_REQUIRED("BKG_05", "Booking Requires An Authenticated Registered User", HttpStatus.UNAUTHORIZED),
    SEAT_ALREADY_BOOKED("BKG_06", "One Or More Selected Seats Are Already Booked", HttpStatus.CONFLICT),
    PAYMENT_FAILED("BKG_07", "Payment Authorisation Failed", HttpStatus.PAYMENT_REQUIRED),
    PERFORMANCE_LOOKUP_FAILED("BKG_08", "Unable To Retrieve Performance Details", HttpStatus.BAD_GATEWAY);

    final String errorCode;
    final String errorDescription;
    final HttpStatus httpStatus;

    ErrorCode(String errorCode, String errorDescription, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
    }

}
