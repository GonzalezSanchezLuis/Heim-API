package com.heim.api.trip.infraestructure.controller;

import com.heim.api.trip.application.dto.TripConfirmationResponse;
import com.heim.api.trip.application.dto.TripRequest;
import com.heim.api.trip.application.service.TripService;
import com.heim.api.trip.domain.entity.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips/")
@CrossOrigin("*")
public class TripController {

    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("confirm")
    public ResponseEntity<TripConfirmationResponse> findNearbyDrivers(@RequestBody TripRequest tripRequest) {
        TripConfirmationResponse response = tripService.confirmTrip(tripRequest);
        return ResponseEntity.ok(response);
    }

    // Endpoint para crear un viaje
    @PostMapping("create")
    public ResponseEntity<Trip> createTrip(@RequestBody TripRequest tripRequest) {
        // Se pasa la información del usuario y el viaje
        Trip trip = tripService.createTrip(tripRequest);
        return ResponseEntity.ok(trip);
    }

    // Endpoint para asignar un conductor a un viaje
    @PutMapping("assign/{tripId}")
    public ResponseEntity<Trip> assignDriver(@PathVariable Long tripId, @RequestParam Long driverId) {
        Trip trip = tripService.assignDriverToTrip(tripId, driverId);
        return trip != null ? ResponseEntity.ok(trip) : ResponseEntity.notFound().build();
    }

    // Endpoint para completar un viaje
    @PutMapping("complete/{tripId}")
    public ResponseEntity<Trip> completeTrip(@PathVariable Long tripId) {
        Trip trip = tripService.completeTrip(tripId);
        return trip != null ? ResponseEntity.ok(trip) : ResponseEntity.notFound().build();
    }

}

