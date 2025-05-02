package com.heim.api.ScheduleMove.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MovingRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservedAt;

    private String moveType;
    private String origin;
    private String destination;
    private BigDecimal originLat;
    private BigDecimal originLng;
    private BigDecimal destinationLat;
    private BigDecimal destinationLng;
    private String status;
    private Long userId;
    private Long driverId;
}
