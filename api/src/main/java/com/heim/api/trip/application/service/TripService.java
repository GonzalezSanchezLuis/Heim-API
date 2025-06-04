package com.heim.api.trip.application.service;

import com.heim.api.drivers.application.dto.DriverLocation;
import com.heim.api.drivers.application.dto.TimeAndDistanceDestinationResponse;
import com.heim.api.drivers.application.dto.TimeAndDistanceOriginResponse;
import com.heim.api.drivers.application.service.DistanceCalculatorService;
import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.drivers.infraestructure.repository.DriverRepository;
import com.heim.api.fcm.domain.entity.FcmToken;
import com.heim.api.hazelcast.application.dto.GeoLocation;
import com.heim.api.hazelcast.service.HazelcastGeoService;
import com.heim.api.notification.application.service.NotificationService;
import com.heim.api.trip.application.dto.TripConfirmationResponse;
import com.heim.api.trip.application.dto.TripRequest;
import com.heim.api.trip.domain.entity.Trip;
import com.heim.api.trip.domain.enums.TripStatus;
import com.heim.api.trip.infraestructure.repository.TripRepository;
import com.heim.api.users.domain.entity.User;
import com.heim.api.users.infraestructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;



@Slf4j
@Service
public class TripService {
    private final TripRepository tripRepository;
    private final NotificationService notificationService;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final HazelcastGeoService hazelcastGeoService;
    private final TripCacheService tripCacheService;
    private final DistanceCalculatorService distanceCalculatorService;


    @Autowired
    public TripService(TripRepository tripRepository,
                       NotificationService notificationService,
                       DriverRepository driverRepository,
                       UserRepository userRepository,
                       HazelcastGeoService hazelcastGeoService,
                       TripCacheService tripCacheService,
                       DistanceCalculatorService distanceCalculatorService
                       ) {

        this.tripRepository = tripRepository;
        this.notificationService = notificationService;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.hazelcastGeoService = hazelcastGeoService;
        this.tripCacheService = tripCacheService;
        this.distanceCalculatorService = distanceCalculatorService;

    }

    private static final int MAX_NOTIFICATION_ATTEMPTS = 3;

    private static final String[] NOTIFICATION_MESSAGES = {
            "¡Nuevo viaje disponible cerca de ti!",
            "¡No pierdas la oportunidad, viaje disponible cerca!",
            "Último aviso: viaje disponible para aceptar.",
    };


    public TripConfirmationResponse confirmTrip(TripRequest tripRequest){
        try {
            double latitude = tripRequest.getOriginLat();
            double longitude = tripRequest.getOriginLng();

            List<Long> nearbyDrivers = Optional.ofNullable(
                    hazelcastGeoService.findNearbyDriversDynamically(latitude, longitude)
            ).orElse(Collections.emptyList());


            if (!nearbyDrivers.isEmpty()){
               // tripCacheService.storeTrip(tripRequest.getUserId(), tripRequest);
                Optional<Trip> existingTrip = tripRepository.findByUser_UserIdAndOriginAndDestinationAndStatus(
                        tripRequest.getUserId(),
                        tripRequest.getOrigin(),
                        tripRequest.getDestination(),
                        TripStatus.REQUESTED
                );
                Trip trip;
                if (existingTrip.isPresent()) {
                    trip = existingTrip.get();
                    log.info("Ya existe un viaje similar para este usuario.");
                } else {
                    trip = createTrip(tripRequest);
                }

                notifyDriversLimited(trip, nearbyDrivers);
                   // notificationService.notify(FcmToken.OwnerType.DRIVER, driverId, "Nuevo viaje disponible", "¡Tienes un nuevo viaje disponible cerca!");

                return new TripConfirmationResponse("Enviando solicitud a conductores cercanos...");
            }else {
                return new TripConfirmationResponse("No hay conductores disponibles cerca por ahora.");
            }

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Trip createTrip(TripRequest tripRequest) {
        User user = userRepository.findById(tripRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + tripRequest.getUserId()));

        Optional<Trip> existingTrip = tripRepository.findByUser_UserIdAndOriginAndDestinationAndStatus(
                user.getUserId(),
                tripRequest.getOrigin(),
                tripRequest.getDestination(),
                TripStatus.REQUESTED
        );

        if (existingTrip.isPresent()) {
            log.info("Ya existe un viaje similar para este usuario.");
            return existingTrip.get(); // Devuelve el viaje existente en lugar de crear uno nuevo
        }

        String rawPrice = String.valueOf(tripRequest.getPrice());  // Ejemplo: "5.843,09 COP"
        String cleanedPrice = rawPrice.replace(".", "").replace(",", ".").replace(" COP", "");
        BigDecimal price = new BigDecimal(cleanedPrice);

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setOrigin(tripRequest.getOrigin());
        trip.setDestination(tripRequest.getDestination());
        trip.setOriginLat(tripRequest.getOriginLat());
        trip.setOriginLng(tripRequest.getOriginLng());
        trip.setDestinationLat(tripRequest.getDestinationLat());
        trip.setDestinationLng(tripRequest.getDestinationLng());
        trip.setTypeOfMove(tripRequest.getTypeOfMove());
        trip.setPrice(price);
        trip.setPaymentMethod(tripRequest.getPaymentMethod());
        trip.setRequestTime(LocalDateTime.now());
        trip.setStatus(TripStatus.REQUESTED);

        trip = tripRepository.save(trip);

        // Notificar al conductor que hay un nuevo viaje disponible
       // notifyDriversAboutNewTrip(trip);

        return trip;
    }

    public Optional<Trip> getTripForDriver(Long tripId, Long driverId) {
        return tripRepository.findByTripIdAndDriver_Id(tripId, driverId);
    }








    public List<Trip> getTripsByUser(Long userId) {
        return tripRepository.findByUser_UserIdAndStatus(userId, TripStatus.REQUESTED);
    }

    public List<Trip> getTripsByDriver(Long driverId, TripStatus status) {
        return tripRepository.findByUser_UserIdAndStatus(driverId, status);
    }



    @Transactional
    public Trip assignDriverToTrip(Long tripId, Long driverId) {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip trip = tripOptional.get();

            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

            trip.setDriver(driver);
            trip.setStatus(TripStatus.ASSIGNED);
            trip.setStartTime(LocalDateTime.now());

            tripRepository.save(trip);

            // Notificar al usuario que el viaje ha sido aceptado por un conductor
            sendNotificationToUser(trip);

            return trip;
        }

        return null;
    }

    @Transactional
    public Trip completeTrip(Long tripId) {
        Optional<Trip> tripOptional = tripRepository.findById(tripId);

        if (tripOptional.isPresent()) {
            Trip trip = tripOptional.get();
            trip.setStatus(TripStatus.COMPLETED);
            trip.setEndTime(LocalDateTime.now());

            tripRepository.save(trip);

            // Notificar al usuario que el viaje ha sido completado
            sendNotificationToUser(trip);

            return trip;
        }

        return null;
    }

    private void notifyDriversAboutNewTrip(Trip trip) {
       // notificationService.notify(FcmToken.OwnerType.DRIVER, trip.getDriver().getId(), "Nuevo viaje disponible", "¡Tienes un nuevo viaje disponible cerca!");
    }

    private void sendNotificationToUser(Trip trip) {
        // Notificar al usuario cuando el viaje cambia de estado
        String message = "";

        switch (trip.getStatus()) {
            case ASSIGNED:
                message = "¡Tu viaje ha sido aceptado por un conductor!";
                break;
            case COMPLETED:
                message = "¡Tu viaje ha sido completado!";
                break;
            default:
                message = "Actualización de tu viaje";
        }

       // notificationService.notify(FcmToken.OwnerType.USER, trip.getUser().getUserId(), "Actualización de tu viaje", "Tu viaje ha sido aceptado.");

    }

    private void notifyDriversLimited(Trip trip, List<Long> nearbyDrivers){
        log.info("Notificando a conductores, total conductores: {}", nearbyDrivers.size());

        // Obtener ubicaciones de conductores
        List<DriverLocation> driverLocations = nearbyDrivers.stream()
                .map(driverId -> {
                    GeoLocation location = hazelcastGeoService.getDriverLocation(driverId);
                    return new DriverLocation(driverId, location.getLatitude(), location.getLongitude());
                })
                .toList();

        // Calcular distancias y tiempos de origen
        Map<Long, TimeAndDistanceOriginResponse> driverDistances = distanceCalculatorService
                .calculateDistancesToUserForMultipleDrivers(driverLocations, trip.getOriginLat(), trip.getOriginLng());


        // Calcular distancias y tiempos de destino
        Map<Long, TimeAndDistanceDestinationResponse> distancesToDestination =
                distanceCalculatorService.calculateDistancesToDestinationForMultipleDrivers(driverLocations, trip.getDestinationLat(), trip.getDestinationLng());

        for (Map.Entry<Long, TimeAndDistanceOriginResponse> entry : driverDistances.entrySet()) {
            Long driverId = entry.getKey();
            TimeAndDistanceOriginResponse response = entry.getValue();

            if (response != null) {
                log.info("Conductor ID {}: distancia = {}, tiempo = {}",
                        driverId, response.getDistance(), response.getEstimatedTimeOfArrival());
            } else {
                log.warn("No se obtuvo distancia/tiempo para el conductor ID {}", driverId);
            }
        }

        for (Long driverId : nearbyDrivers){
            int attempts = tripCacheService.getNotificationCount(trip.getTripId(), driverId);
            if (attempts < MAX_NOTIFICATION_ATTEMPTS){
                String message = NOTIFICATION_MESSAGES[attempts];
                log.info("Notificando conductor con ID: {}", driverId);

                Map<String, String> tripData = buildTripData(trip, driverDistances.get(driverId),distancesToDestination.get(driverId));


                notificationService.notify(
                        FcmToken.OwnerType.DRIVER,
                        driverId,
                        "Nuevo viaje disponible",         // Título
                        "Tienes un nuevo viaje",          // Cuerpo (podrías personalizarlo más)
                        tripData,                         // Datos
                        message                           // Mensaje adicional
                );
                tripCacheService.incrementNotificationCount(trip.getTripId(), driverId);
            }else {
                log.info("No se notificará más al conductor {} para el viaje {} para evitar spam.", driverId, trip.getTripId());
            }
        }
    }

    private Map<String, String> buildTripData(
            Trip trip,
            TimeAndDistanceOriginResponse distanceResponse,
            TimeAndDistanceDestinationResponse destinationResponse) {
        Map<String, String> tripData = new HashMap<>();
        tripData.put("tripId", String.valueOf(trip.getTripId()));
        tripData.put("origin", trip.getOrigin());
        tripData.put("destination", trip.getDestination());
        tripData.put("originLat", String.valueOf(trip.getOriginLat()));
        tripData.put("originLng", String.valueOf(trip.getOriginLng()));
        tripData.put("destinationLat", String.valueOf(trip.getDestinationLat()));
        tripData.put("destinationLng", String.valueOf(trip.getDestinationLng()));
        tripData.put("typeOfMove", String.valueOf(trip.getTypeOfMove()));
        tripData.put("price", trip.getPrice().toPlainString());
        tripData.put("paymentMethod", String.valueOf(trip.getPaymentMethod()));

        log.info("DATOS DE LA DISTANCIA {}" , distanceResponse);

        tripData.put("distance", distanceResponse.getDistance());
        tripData.put("estimatedTimeOfArrival", distanceResponse.getEstimatedTimeOfArrival());

        tripData.put("distanceToDestination", destinationResponse.getDistanceToDestination());
        tripData.put("timeToDestination", destinationResponse.getTimeToDestination());
        return tripData;
    }


 

   /* private boolean isDriverAvailable(Long driverId){
        List<Trip> activeTrips = tripRepository.findByUser_UserIdAndStatus(driverId, TripStatus.IN_PROGRESS);
        return activeTrips.isEmpty();
    }

    public List<Long> findNearbyDrivers(double latitude, double longitude) {
        return hazelcastGeoService.findNearbyDriversDynamically(latitude, longitude);
    }*/
    }
