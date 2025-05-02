package com.heim.api.users.application.mapper;

import com.heim.api.ScheduleMove.application.dto.MovingResponse;
import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import com.heim.api.users.application.dto.UserRequest;
import com.heim.api.users.application.dto.UserResponse;
import com.heim.api.users.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "reservationMovingList", expression = "java(mapReservations(user.getReservationMovingList()))")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "active", source = "active")
    UserResponse toResponse(User user);

    @Mapping(target = "userId", ignore = true) // Se ignora porque es autogenerado
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reservationMovingList", ignore = true)
    @Mapping(target = "active", source = "active")
    User toEntity(UserRequest userRequest);

    default List<MovingResponse> mapReservations(List<ReservationMoving> reservations) {
        if (reservations == null) return Collections.emptyList();
        return reservations.stream()
                .map(reservation -> new MovingResponse(reservation.getReservationMovingId(), reservation.getStatus()))  // Mapea los datos necesarios
                .collect(Collectors.toList());
    }
}

