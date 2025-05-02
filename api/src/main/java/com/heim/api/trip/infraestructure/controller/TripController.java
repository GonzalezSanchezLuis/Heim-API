package com.heim.api.trip.infraestructure.controller;

import com.heim.api.trip.application.dto.TripRequest;
import com.heim.api.trip.application.service.TripService;
import com.heim.api.trip.domain.entity.Trip;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers/")
@CrossOrigin("*")
public class TripController {

    private TripService tripService;

    public  TripController(TripService tripService){
        this.tripService = tripService;

    }

    @PostMapping("/trips")
    public ResponseEntity<Trip> createTrip(@RequestBody TripRequest request) {
        Trip trip = tripService.createTrip(
                request.getUserId(),
                request.getDriverId(),
                request.getOriginLatitude(),
                request.getOriginLatitude(),
                request.getDestinationLatitude(),
                request.getDestinationLongitude()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }

}
