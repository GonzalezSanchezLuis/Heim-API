package com.heim.api.ScheduleMove.infraestructure.repository;

import com.heim.api.ScheduleMove.domain.entity.ReservationMoving;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReservationMovingRepository extends CrudRepository<ReservationMoving, Long> {
    Optional<ReservationMoving> findById(Long id);
}
