package com.heim.api.trip.application.dto;

public class TripConfirmationResponse {
    private String message;

    public TripConfirmationResponse(String message) {
        this.message = message;
    }

    // Getter
    public String getMessage() {
        return message;
    }
}
