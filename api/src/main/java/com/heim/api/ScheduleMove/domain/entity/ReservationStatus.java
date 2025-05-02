package com.heim.api.ScheduleMove.domain.entity;

public enum ReservationStatus {
    PENDING,      // Esperando que el conductor acepte
    ACCEPTED,     // Conductor aceptó la mudanza
    COMPLETED,    // Mudanza finalizada
    CANCELED
}
