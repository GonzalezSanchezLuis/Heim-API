package com.heim.api.drivers.application.dto;

import lombok.Data;
@Data
public class DriverRequest {
    private String fullName;
    private String email;
    private String password;
    private String  document;
    private String  phone;
    private String urlAvatarProfile;
    private String  licenseNumber;
    private String  vehicleType;
    private String  enrollVehicle;
    private boolean active;
    private String role;
}
