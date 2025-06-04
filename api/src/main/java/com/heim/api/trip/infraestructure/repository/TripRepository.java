package com.heim.api.trip.infraestructure.repository;

import com.heim.api.trip.domain.entity.Trip;
import com.heim.api.trip.domain.enums.TripStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends CrudRepository<Trip, Long> {
    Optional<Trip> findByTripIdAndDriver_Id(Long tripId, Long driverId);
    List<Trip> findByDriverIdAndStatus(Long driverId, TripStatus status);
    List<Trip> findByUser_UserIdAndStatus(Long userId, TripStatus status);

    Optional<Trip> findByUser_UserIdAndOriginAndDestinationAndStatus(Long userId, String origin, String destination, TripStatus status);


}

