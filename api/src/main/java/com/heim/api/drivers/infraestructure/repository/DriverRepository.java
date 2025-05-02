package com.heim.api.drivers.infraestructure.repository;

import com.heim.api.drivers.domain.entity.Driver;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends CrudRepository<Driver, Long> {
    Optional<Driver> findDriverByEmail(String email);

    @Query("SELECT d.id FROM Driver d WHERE d.status = 'CONNECTED'")
    List<Long> findConnectedDriverIds();
}
