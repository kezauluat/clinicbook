package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.auca.clinicbook.entity.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUserId(Long userId);

    @Query("""
           select p from Patient p join fetch p.user u
           where lower(u.fullName) like lower(concat('%', :q, '%'))
              or lower(u.email) like lower(concat('%', :q, '%'))
              or coalesce(u.phone, '') like concat('%', :q, '%')
           order by u.fullName
           """)
    List<Patient> search(@Param("q") String q);
}
