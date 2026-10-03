package rw.auca.clinicbook.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.entity.*;
import rw.auca.clinicbook.repository.*;

import java.time.LocalTime;

/** Seeds an admin plus demo data (clinic, departments, doctor with hours, receptionist) on an empty database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClinicRepository clinicRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@clinicbook.rw}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        ensureUser("System Admin", adminEmail, adminPassword, null, RoleName.ADMIN);
        if (clinicRepository.count() > 0) {
            return;
        }
        Clinic clinic = clinicRepository.save(Clinic.builder()
                .name("Kigali Family Clinic").address("KN 3 Rd, Kigali").phone("+250788100100").build());
        Department general = departmentRepository.save(Department.builder().clinic(clinic).name("General Medicine").build());
        departmentRepository.save(Department.builder().clinic(clinic).name("Pediatrics").build());
        departmentRepository.save(Department.builder().clinic(clinic).name("Dentistry").build());

        User doctorUser = ensureUser("Dr. Aline Uwase", "doctor@clinicbook.rw", "Doctor@12345", "+250788100101", RoleName.DOCTOR);
        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(doctorUser).department(general).specialization("General Practitioner").licenseNo("RMDC-1001").build());
        for (int day = 1; day <= 5; day++) {
            availabilityRepository.save(Availability.builder().doctor(doctor).dayOfWeek(day)
                    .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build());
            availabilityRepository.save(Availability.builder().doctor(doctor).dayOfWeek(day)
                    .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(17, 0)).build());
        }
        ensureUser("Grace Mukamana", "reception@clinicbook.rw", "Reception@12345", "+250788100102", RoleName.RECEPTIONIST);
        log.info("Seeded demo clinic, departments, doctor and receptionist");
    }

    private User ensureUser(String name, String email, String password, String phone, RoleName role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = User.builder()
                    .fullName(name).email(email).phone(phone)
                    .passwordHash(passwordEncoder.encode(password))
                    .build();
            user.getRoles().add(roleRepository.findByName(role).orElseThrow());
            return userRepository.save(user);
        });
    }
}
