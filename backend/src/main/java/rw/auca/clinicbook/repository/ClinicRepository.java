package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    boolean existsByName(String name);
}
