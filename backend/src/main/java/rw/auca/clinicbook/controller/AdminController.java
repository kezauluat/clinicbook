package rw.auca.clinicbook.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.service.AdminService;
import rw.auca.clinicbook.service.ClinicService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ClinicService clinicService;

    @PostMapping("/clinics")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicResponse createClinic(@Valid @RequestBody ClinicRequest request) {
        return clinicService.createClinic(request);
    }

    @PostMapping("/clinics/{clinicId}/departments")
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse createDepartment(@PathVariable Long clinicId, @Valid @RequestBody DepartmentRequest request) {
        return clinicService.createDepartment(clinicId, request);
    }

    @PostMapping("/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        return adminService.createDoctor(request);
    }

    @PostMapping("/receptionists")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createReceptionist(@Valid @RequestBody CreateStaffRequest request) {
        return adminService.createReceptionist(request);
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return adminService.listUsers();
    }

    @PatchMapping("/users/{id}/enabled")
    public AdminUserResponse setEnabled(@PathVariable Long id, @RequestParam boolean value) {
        return adminService.setEnabled(id, value);
    }
}
