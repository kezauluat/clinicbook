package rw.auca.clinicbook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.clinicbook.dto.ClinicResponse;
import rw.auca.clinicbook.dto.DepartmentResponse;
import rw.auca.clinicbook.service.ClinicService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping("/clinics")
    public List<ClinicResponse> clinics() {
        return clinicService.listClinics();
    }

    @GetMapping("/departments")
    public List<DepartmentResponse> departments() {
        return clinicService.listDepartments();
    }
}
