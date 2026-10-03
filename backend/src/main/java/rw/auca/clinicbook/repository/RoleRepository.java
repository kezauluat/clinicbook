package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.Role;
import rw.auca.clinicbook.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
