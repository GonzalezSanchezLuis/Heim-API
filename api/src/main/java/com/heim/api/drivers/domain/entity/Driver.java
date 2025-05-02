package com.heim.api.drivers.domain.entity;

import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import com.heim.api.drivers.domain.enums.DriverStatus;
import com.heim.api.trip.domain.entity.Trip;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drivers")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Driver {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long driverId;
    private String fullName;
    private String email;
    private String password;
    private String  document;
    private String  phone;
    private String  licenseNumber;
    private String  vehicleType;
    private String  enrollVehicle;
    private String role;
    private LocalDateTime createdAt;
    private boolean isActive;
    private String urlAvatarProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DriverStatus status;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }



    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL) // Relación con Trip
    private List<Trip> trips = new ArrayList<>();

    @Override
    public String toString() {
        return "Driver{" +
                "driverId=" + driverId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                // Omitir la lista de 'reservationMovingList'
                '}';
    }

}
