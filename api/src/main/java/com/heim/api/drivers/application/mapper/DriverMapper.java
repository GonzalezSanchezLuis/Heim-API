package com.heim.api.drivers.application.mapper;

import com.heim.api.drivers.application.dto.DriverRequest;
import com.heim.api.drivers.application.dto.DriverResponse;
import com.heim.api.drivers.domain.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "driverId", ignore = true)
    @Mapping(target = "createdAt", source ="createdAt")
    @Mapping(target = "active", source = "active")
    DriverResponse toResponse(Driver driver);

    @Mapping(target = "driverId", ignore = true) // Se ignora porque es autogenerado
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", source = "active", ignore = true)
    Driver toEntity(DriverRequest driverRequest);
}
