package com.theatre.bookingservice.config;

public record AuthenticatedUser(String subject, String email, String role) {

    public String displayName() {
        return email != null ? email : subject;
    }

}
