package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.entity.*;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @CacheEvict(cacheNames = "doctors", allEntries = true)
    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest req) {
        Department dep = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> ApiException.notFound("Department"));
        User user = createUser(req.fullName(), req.email(), req.password(), req.phone(), RoleName.DOCTOR);
        Doctor doctor = doctorRepository.saveAndFlush(Doctor.builder()
                .user(user).department(dep).specialization(req.specialization()).licenseNo(req.licenseNo().trim())
                .build());
        return DoctorResponse.from(doctor);
    }

    @Transactional
    public AdminUserResponse createReceptionist(CreateStaffRequest req) {
        return AdminUserResponse.from(createUser(req.fullName(), req.email(), req.password(), req.phone(), RoleName.RECEPTIONIST));
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll(Sort.by("id")).stream().map(AdminUserResponse::from).toList();
    }

    @Transactional
    public AdminUserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        user.setEnabled(enabled);
        return AdminUserResponse.from(user);
    }

    private User createUser(String fullName, String email, String password, String phone, RoleName role) {
        String normalized = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = User.builder()
                .fullName(fullName.trim()).email(normalized).phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .build();
        user.getRoles().add(roleRepository.findByName(role).orElseThrow());
        return userRepository.save(user);
    }
}
