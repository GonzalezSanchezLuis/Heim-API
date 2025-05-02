package com.heim.api.trip.domain.entity;

import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.trip.domain.enums.TripStatus;
import com.heim.api.users.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "trip")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tripId;
    private TripStatus status;
    private LocalDateTime requestTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Relación con Usuario
    private User user;

    @ManyToOne
    @JoinColumn(name = "driver", nullable = true) // Puede ser null si aún no hay conductor asignado
    private Driver driver;

}
