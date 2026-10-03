package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.Department;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByClinicId(Long clinicId);
}
