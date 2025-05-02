package com.heim.api.ScheduleMove.application.dto;

import com.heim.api.drivers.application.dto.DriverResponse;
import com.heim.api.users.application.dto.UserResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MovingResponse {
    private Long reservationMovingId;
    private LocalDateTime reservedAt;
    private String origin;
    private String destination;
    private String moveType;
    private BigDecimal originLat;
    private BigDecimal originLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private Long userId;
    private String status;

    private UserResponse userResponse;
    private DriverResponse driverResponse;

    public MovingResponse(Long reservationMovingId, String status) {
    }
}

