package com.theatre.bookingservice.exception;

import com.theatre.bookingservice.model.CommonResponse;
import com.theatre.bookingservice.util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<CommonResponse> handleServiceException(ServiceException ex) {
        CommonResponse commonResponse = CommonResponse.builder()
                .statusCode(ex.getErrorCode().orElse(ErrorCode.DEFAULT.getErrorCode()))
                .statusDescription(ex.getErrorDescription().orElse(ErrorCode.DEFAULT.getErrorDescription()))
                .build();
        return ResponseEntity.status(ex.getHttpStatus()).body(commonResponse);
    }
}
