package com.heim.api.ScheduleMove.application.mapper.helper;

import com.heim.api.drivers.domain.entity.Driver;
import com.heim.api.users.domain.entity.User;
import org.mapstruct.Named;

public class MovingMapperHelper {
    @Named("mapUser")
    public static User mapUser(Long userId) {
        if (userId == null) return null;
        User user = new User();
        user.setUserId(userId);
        return user;
    }

    @Named("mapDriver")
    public static Driver mapDriver(Long driverId) {
        if (driverId == null) return null;
        Driver driver = new Driver();
        driver.setDriverId(driverId);
        return driver;
    }
}
