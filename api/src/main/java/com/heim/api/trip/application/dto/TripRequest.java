package com.heim.api.trip.application.dto;

import lombok.Data;

@Data
public class TripRequest {
    private Long userId;
    private Long driverId;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
}
