package com.heim.api.drivers.application.dto;

import com.heim.api.drivers.domain.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatusResponse {
    private Long driverId;
    private DriverStatus status;


    public DriverStatusResponse(Long driverId, String name) {
    }
}
