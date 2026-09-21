package com.theatre.bookingservice.exception;

import com.theatre.bookingservice.util.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Optional;

public class ServiceException extends RuntimeException {

    private final String errorCode;
    private final String errorDescription;
    private final HttpStatus httpStatus;

    public ServiceException(String errorCode, String errorDescription) {
        super(errorDescription);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getErrorDescription());
        this.errorCode = errorCode.getErrorCode();
        this.errorDescription = errorCode.getErrorDescription();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public Optional<String> getErrorCode() {
        return Optional.ofNullable(this.errorCode);
    }

    public Optional<String> getErrorDescription() {
        return Optional.ofNullable(this.errorDescription);
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

}
