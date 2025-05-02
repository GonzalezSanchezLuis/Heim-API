package com.heim.api.trip.infraestructure.repository;

import com.heim.api.trip.domain.entity.Trip;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TripRepository extends CrudRepository<Trip, Long> {
    Optional<Trip> findByTripId(Long tripId); // Usa 'findByTripId' en lugar de 'findTripById'
}

