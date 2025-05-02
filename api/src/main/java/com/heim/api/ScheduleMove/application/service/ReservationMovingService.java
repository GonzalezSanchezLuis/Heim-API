package com.heim.api.ScheduleMove.application.service;

import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.drivers.infraestructure.repository.DriverRepository;
import com.heim.api.ScheduleMove.application.dto.MovingRequest;
import com.heim.api.ScheduleMove.application.dto.MovingResponse;
import com.heim.api.ScheduleMove.application.mapper.MovingMapper;
import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import com.heim.api.ScheduleMove.infraestructure.repository.ReservationMovingRepository;
import com.heim.api.price.service.PriceService;
import com.heim.api.users.domain.entity.User;
import com.heim.api.users.infraestructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Transactional
public class ReservationMovingService {
    private final ReservationMovingRepository reservationMovingRepository;
    private final MovingMapper movingMapper;
    private final UserRepository userRepository;
    private final DriverRepository  driverRepository;
    private final PriceService priceService;


    @Autowired
    public  ReservationMovingService(
            ReservationMovingRepository  reservationMovingRepository,
            MovingMapper movingMapper,
            UserRepository userRepository,
            DriverRepository driverRepository,
            PriceService priceService){

        this.reservationMovingRepository = reservationMovingRepository;
        this.movingMapper = movingMapper;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.priceService = priceService;
    }

    public MovingResponse savedReservationMoving(MovingRequest movingRequest){
        ReservationMoving newReservationMoving = movingMapper.toEntity(movingRequest);

        User user = userRepository.findById(movingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Driver driver = driverRepository.findById(movingRequest.getDriverId())
                .orElseThrow(() -> new RuntimeException("Conductor  no encontrado"));

        newReservationMoving.setUser(user);
        newReservationMoving.setDriver(driver);
        
        ReservationMoving savedReservation = reservationMovingRepository.save(newReservationMoving);
        System.out.println("DATOS DEL CODUCTOR" + driver);
        return movingMapper.toResponse(savedReservation);

    }

    public MovingResponse reservationMovingById(Long reservationMovingId) throws NoSuchElementException {
        ReservationMoving reservationMoving = reservationMovingRepository.findById(reservationMovingId).orElseThrow(()
                -> new NoSuchElementException("Información no encontrada"));

        return  movingMapper.toResponse(reservationMoving);

    }


}
