package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.Availability;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);
    List<Availability> findByDoctorIdAndDayOfWeek(Long doctorId, Integer dayOfWeek);
}
