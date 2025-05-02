package com.heim.api.trip.domain.enums;


public enum TripStatus {
    REQUESTED, // Usuario solicita el viaje
    ASSIGNED,  // Conductor asignado
    IN_PROGRESS, // En curso
    COMPLETED, // Finalizado
    CANCELED // Cancelado
}
