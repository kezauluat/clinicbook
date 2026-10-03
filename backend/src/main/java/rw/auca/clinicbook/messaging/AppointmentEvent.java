package rw.auca.clinicbook.messaging;

import rw.auca.clinicbook.entity.Appointment;
import rw.auca.clinicbook.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Message sent through RabbitMQ. Contains everything the notification consumers need. */
public record AppointmentEvent(String type, Long appointmentId,
                               String patientName, String patientEmail, String patientPhone,
                               String doctorName, String departmentName, LocalDateTime startAt) implements Serializable {

    public static AppointmentEvent of(String type, Appointment a) {
        User patient = a.getPatient().getUser();
        User doctor = a.getDoctor().getUser();
        return new AppointmentEvent(type, a.getId(), patient.getFullName(), patient.getEmail(), patient.getPhone(),
                doctor.getFullName(), a.getDoctor().getDepartment().getName(), a.getStartAt());
    }
}
