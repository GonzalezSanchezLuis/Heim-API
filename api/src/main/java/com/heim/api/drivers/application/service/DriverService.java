package com.heim.api.drivers.application.service;

import com.heim.api.drivers.application.dto.*;
import com.heim.api.drivers.application.mapper.DriverMapper;
import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.drivers.domain.enums.DriverStatus;
import com.heim.api.drivers.infraestructure.repository.DriverRepository;
import com.heim.api.fcm.domain.entity.FcmToken;
import com.heim.api.fcm.infraestructure.repository.FcmTokenRepository;
import com.heim.api.hazelcast.service.HazelcastGeoService;
import com.heim.api.users.domain.entity.User;
import com.heim.api.users.infraestructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class DriverService {
    private static final Logger logger = LoggerFactory.getLogger(DriverService.class);
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final HazelcastGeoService hazelcastGeoService;
    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Autowired
    public DriverService(
            DriverRepository driverRepository,
            DriverMapper driverMapper,
            HazelcastGeoService hazelcastGeoService,
            UserRepository  userRepository,
            FcmTokenRepository fcmTokenRepository){
        this.driverMapper = driverMapper;
        this.hazelcastGeoService = hazelcastGeoService;
        this.userRepository = userRepository;
        this.driverRepository= driverRepository;
        this.fcmTokenRepository =  fcmTokenRepository;

    }

    public DriverResponse registerDriver(DriverRequest driverRequest) throws Exception {
        Optional<User> userOptional = userRepository.findById(driverRequest.getUserId());
        if (userOptional.isEmpty()) {
            throw new Exception("No existe un usuario con ese ID.");
        }

        User user = userOptional.get();

        // Verificar si ya existe un conductor para este usuario
        Optional<Driver> existingDriver = driverRepository.findByUserId(driverRequest.getUserId());
        if (existingDriver.isPresent()) {
            throw new Exception("Este usuario ya está registrado como conductor.");
        }

           user.setRole("DRIVER");
           user.setActive(false);

            Driver newDriver = driverMapper.toEntity(driverRequest);
            newDriver.setStatus(DriverStatus.DISCONNECTED);
            newDriver.setUser(user);

            fcmTokenRepository.findByOwnerIdAndOwnerType(user.getUserId(), FcmToken.OwnerType.USER).ifPresent(
                    fcmToken -> {
                        fcmToken.setOwnerType(FcmToken.OwnerType.DRIVER);
                        fcmTokenRepository.save(fcmToken);
                    }
            );

           Driver savedDriver = driverRepository.save(newDriver);

        System.out.println("Mapped driver: " + newDriver);
        return driverMapper.toResponse(savedDriver);
    }

    public DriverResponse getDriverById(Long driverId) throws NoSuchElementException {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        return driverMapper.toResponse(driver);
    }

    public DriverResponse updatedDriverData(Long driverId, DriverRequest driverRequest) {
        // Buscar el usuario por ID
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));


        driver.setVehicleType(driverRequest.getVehicleType());
        driver.setEnrollVehicle(driverRequest.getEnrollVehicle());



        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }


    public void driverDelete(Long driverId){
        Driver driverToDelete = driverRepository.findById(driverId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        driverRepository.delete(driverToDelete);

    }



    public Driver updateDriverStatus(Long driverId, DriverStatusRequest request) {
        Driver driver = findDriverById(driverId);

        driver.setStatus(DriverStatus.CONNECTED);
        driverRepository.save(driver);

        logger.info("📡 Estado del conductor actualizado: {}", driver);

        // Manejar la ubicación en Hazelcast según el estado del conductor
        handleDriverLocation(driverId, DriverStatus.CONNECTED, request.getLatitude(), request.getLongitude());

        return driver;
    }



    public Driver driverDisconnected(Long driverId, DriverStatusDisconnectedRequest request) {
        Driver driver = findDriverById(driverId);

        driver.setStatus(DriverStatus.DISCONNECTED);
        driverRepository.save(driver);

        // Llamar a handleDriverLocation para manejar la ubicación
        handleDriverLocation(driverId, DriverStatus.DISCONNECTED, null, null);

        logger.info("🔴 Conductor {} desconectado", driverId);

        return driver;
    }


    private Driver findDriverById(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }


    public DriverStatusResponse getDriverStatus(Long userId){
        Driver driver = driverRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        logger.info("estado delcondcutor" +driver.getStatus());
        return new DriverStatusResponse(driver.getId(), driver.getStatus());
    }


    public boolean isDriverAvailable(Long driverId) throws ChangeSetPersister.NotFoundException {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        return driver != null && driver.getStatus() == DriverStatus.CONNECTED;

    }

    public void updateDriverLocation(DriverUpdateLocationRequest driverUpdateLocationRequest, Long driverId) {
        Double latitude = driverUpdateLocationRequest.getLatitude();
        Double longitude = driverUpdateLocationRequest.getLongitude();

        if (latitude != null && longitude != null) {
            hazelcastGeoService.updateDriverLocation(driverId, latitude, longitude);
            logger.info("✅ Ubicación almacenada en Hazelcast para el conductor {} -> ({}, {})", driverId, latitude, longitude);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas no proporcionadas para conductor conectado");
        }
    }



    private void handleDriverLocation(Long driverId, DriverStatus status, Double latitude, Double longitude) {
        if (status == DriverStatus.CONNECTED) {
            if (latitude != null && longitude != null) {
                hazelcastGeoService.updateDriverLocation(driverId, latitude, longitude);
                logger.info("✅ Ubicación almacenada en Hazelcast para el conductor {} -> ({}, {})", driverId, latitude, longitude);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas no proporcionadas para conductor conectado");
            }
        } else if (status == DriverStatus.DISCONNECTED) {
            hazelcastGeoService.removeDriverLocation(driverId);
            logger.info("🚫 Ubicación eliminada de Hazelcast para el conductor {}", driverId);
        }
    }



}
