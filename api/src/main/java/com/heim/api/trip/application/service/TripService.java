package com.heim.api.trip.application.service;

import com.heim.api.drivers.application.service.DriverService;
import com.heim.api.trip.domain.entity.Trip;
import com.heim.api.trip.domain.enums.TripStatus;
import com.heim.api.trip.exception.BusinessException;
import com.heim.api.trip.infraestructure.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

@Service
public class TripService {
        private final TripRepository tripRepository;
        private final DriverService driverService;

        @Autowired
        public  TripService(TripRepository tripRepository, DriverService driverService){
            this.tripRepository = tripRepository;
            this.driverService = driverService;

        }

        public Trip createTrip(Long userId, Long driverId, Double originLat, Double originLng, Double destLat, Double destLng) {
            Trip trip = new Trip();
            trip.setStatus(TripStatus.REQUESTED);
            trip.setOriginLatitude(originLat);
            trip.setOriginLongitude(originLng);
            trip.setDestinationLatitude(destLat);
            trip.setDestinationLongitude(destLng);
            return tripRepository.save(trip);
        }

        public Trip assignDriverToTrip(Long tripId, Long driverId) throws ChangeSetPersister.NotFoundException {
            Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new ChangeSetPersister.NotFoundException());
            if (!driverService.isDriverAvailable(driverId)) {
                throw new BusinessException("El conductor no está disponible", "DRIVER_NOT_AVAILABLE");
            }


            trip.setStatus(TripStatus.ASSIGNED);
            return tripRepository.save(trip);
        }
    }


