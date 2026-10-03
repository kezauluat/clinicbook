package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.auca.clinicbook.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    @Query("""
           select d from Doctor d
           join fetch d.user u
           join fetch d.department dep
           where (:departmentId is null or dep.id = :departmentId)
             and (lower(u.fullName) like lower(concat('%', :q, '%'))
                  or lower(coalesce(d.specialization, '')) like lower(concat('%', :q, '%')))
           order by u.fullName
           """)
    List<Doctor> search(@Param("departmentId") Long departmentId, @Param("q") String q);
}
