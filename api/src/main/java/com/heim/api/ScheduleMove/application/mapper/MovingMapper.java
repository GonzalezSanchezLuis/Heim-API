package com.heim.api.ScheduleMove.application.mapper;

import com.heim.api.drivers.application.dto.DriverResponse;
import com.heim.api.ScheduleMove.application.dto.MovingRequest;
import com.heim.api.ScheduleMove.application.dto.MovingResponse;
import com.heim.api.ScheduleMove.application.mapper.helper.MovingMapperHelper;
import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.users.application.dto.UserResponse;
import com.heim.api.users.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MovingMapperHelper.class)
public interface MovingMapper {


        @Mapping(target = "reservationMovingId", ignore = true) // Se ignora porque es autogenerado
        @Mapping(target = "user", source = "userId", qualifiedByName = "mapUser")
        @Mapping(target = "driver", source = "driverId", qualifiedByName = "mapDriver")
        ReservationMoving toEntity(MovingRequest movingRequest);

        @Mapping(target = "reservationMovingId", source = "reservationMovingId")
        @Mapping(target = "userId", source = "user.userId")
        @Mapping(target = "userResponse", expression = "java(mapUserToResponse(reservationMoving.getUser()))")
        @Mapping(target = "driverResponse", expression = "java(mapDriverToResponse(reservationMoving.getDriver()))")
        @Mapping(target = "status", source = "status")
        @Mapping(target = "moveType", source = "moveType")
        MovingResponse toResponse(ReservationMoving reservationMoving);

        default UserResponse mapUserToResponse(User user) {
            if (user == null) return null;
            return new UserResponse(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getDocument(),
                    user.getUrlAvatarProfile(),
                    user.getRole(),
                    user.getCreatedAt().toString(),
                    user.isActive(), null);
        }

        default DriverResponse mapDriverToResponse(Driver driver) {
            if (driver == null) return null;
            return new DriverResponse(
                    driver.getDriverId(),
                    driver.getFullName(),
                    driver.getEmail(),
                    driver.getPhone(),
                    driver.getUrlAvatarProfile(),
                    driver.getDocument(),
                    driver.getLicenseNumber(),
                    driver.getVehicleType(),
                    driver.getEnrollVehicle(),
                    driver.getRole(),
                    driver.getCreatedAt(),
                    driver.isActive(), null);
        }

        @Mapping(target = "userId", source = "userId")
        default User mapUser(Long userId) {
            if (userId == null) return null;
            User user = new User();
            user.setUserId(userId);
            return user;
        }

        @Mapping(target = "driverId", source = "driverId")
        default Driver mapDriver(Long driverId) {
            if (driverId == null) return null;
            Driver driver = new Driver();
            driver.setDriverId(driverId);
            return driver;
        }
    }

