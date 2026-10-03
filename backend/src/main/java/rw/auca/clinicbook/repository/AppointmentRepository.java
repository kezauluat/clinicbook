package rw.auca.clinicbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.auca.clinicbook.entity.Appointment;
import rw.auca.clinicbook.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByStartAtDesc(Long patientId);

    List<Appointment> findByDoctorIdAndStartAtBetweenOrderByStartAt(Long doctorId, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByStartAtBetweenOrderByStartAt(LocalDateTime from, LocalDateTime to);

    boolean existsByDoctorIdAndStartAtAndStatusNot(Long doctorId, LocalDateTime startAt, AppointmentStatus status);

    List<Appointment> findByStatusAndStartAtBetween(AppointmentStatus status, LocalDateTime from, LocalDateTime to);
}
