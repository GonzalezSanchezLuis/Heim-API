package com.heim.api.ScheduleMove.controller;

import com.heim.api.ScheduleMove.application.dto.MovingRequest;
import com.heim.api.ScheduleMove.application.dto.MovingResponse;
import com.heim.api.ScheduleMove.application.service.ReservationMovingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/reservations/")
public class ReservationMovingController {
    private final ReservationMovingService reservationMovingService;

    @Autowired
    public ReservationMovingController(ReservationMovingService reservationMovingService){
        this.reservationMovingService = reservationMovingService;
    }

    @PostMapping("reservation")
    public ResponseEntity<?> reservationMoving(@RequestBody MovingRequest movingRequest){
        try{
            MovingResponse reservationSaved = reservationMovingService.savedReservationMoving(movingRequest);
            return  ResponseEntity.ok(reservationSaved);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("reservation/{reservationId}")
    public  ResponseEntity<MovingResponse> getReservationMovingById(@PathVariable Long reservationId){
        try {
            MovingResponse movingResponse = reservationMovingService.reservationMovingById(reservationId);
            return  new ResponseEntity<>(movingResponse,HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
