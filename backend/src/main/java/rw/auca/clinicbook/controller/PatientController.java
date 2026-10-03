package rw.auca.clinicbook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rw.auca.clinicbook.dto.PatientResponse;
import rw.auca.clinicbook.service.PatientService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    public List<PatientResponse> search(@RequestParam(required = false) String q) {
        return patientService.search(q);
    }
}
