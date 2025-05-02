package com.heim.api.drivers.application.service;

import com.heim.api.drivers.application.dto.*;
import com.heim.api.drivers.application.mapper.DriverMapper;
import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.drivers.domain.enums.DriverStatus;
import com.heim.api.drivers.infraestructure.repository.DriverRepository;
import com.heim.api.hazelcast.service.HazelcastGeoService;
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
    private final BCryptPasswordEncoder bcryptPasswordEncoder;
    private final HazelcastGeoService hazelcastGeoService;

    @Autowired
    public DriverService(@Lazy DriverRepository driverRepository,
                         DriverMapper driverMapper,
                         BCryptPasswordEncoder bCryptPasswordEncoder,
                         HazelcastGeoService hazelcastGeoService){
        this.driverMapper = driverMapper;
        this.driverRepository = driverRepository;
        this.bcryptPasswordEncoder = bCryptPasswordEncoder;
        this.hazelcastGeoService = hazelcastGeoService;

    }

    public DriverResponse registerDriver(DriverRequest driverRequest) throws Exception {
        Optional<Driver> existingDriver = driverRepository.findDriverByEmail(driverRequest.getEmail());
        if (existingDriver.isPresent()) {
            throw new Exception("Ya existe un usuario registrado con ese email.");
        } else {
            String encodePassword = bcryptPasswordEncoder.encode(driverRequest.getPassword());
            driverRequest.setPassword(encodePassword);
            Driver newDriver = driverMapper.toEntity(driverRequest);
            newDriver.setActive(false);
            newDriver.setRole("DRIVER");

            System.out.println("Mapped driver: " + newDriver);
            return driverMapper.toResponse(driverRepository.save(newDriver));

        }
    }

    public DriverResponse getDriverById(Long driverId) throws NoSuchElementException {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        return driverMapper.toResponse(driver);
    }

    public DriverResponse updatedDriverData(Long driverId, DriverRequest driverRequest) {
        // Buscar el usuario por ID
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if(driverRequest.getPassword() != null && !driverRequest.getPassword().isEmpty()){
            driver.setPassword(bcryptPasswordEncoder.encode(driverRequest.getPassword()));
        }
        driver.setFullName(driverRequest.getFullName());
        driver.setEmail(driverRequest.getEmail());
        driver.setPhone(driverRequest.getPhone());
        driver.setVehicleType(driverRequest.getVehicleType());
        driver.setEnrollVehicle(driverRequest.getEnrollVehicle());
        driver.setDocument(driverRequest.getDocument());
        driver.setUrlAvatarProfile(driverRequest.getUrlAvatarProfile());


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


    public DriverStatusResponse getDriverStatus(Long driverId){
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Conductor no encontrado"));
        return new DriverStatusResponse(driver.getDriverId(), driver.getStatus().name());

    }


    public boolean isDriverAvailable(Long driverId) throws ChangeSetPersister.NotFoundException {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        return driver != null && driver.getStatus() == DriverStatus.CONNECTED;

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
