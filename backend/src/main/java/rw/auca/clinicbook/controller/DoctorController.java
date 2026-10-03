package rw.auca.clinicbook.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import rw.auca.clinicbook.dto.AvailabilityResponse;
import rw.auca.clinicbook.dto.DoctorResponse;
import rw.auca.clinicbook.dto.SlotResponse;
import rw.auca.clinicbook.service.AvailabilityService;
import rw.auca.clinicbook.service.DoctorService;

import java.time.LocalDate;
import java.util.List;

/** Public: anyone can search doctors and see free slots. */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;

    @GetMapping
    public List<DoctorResponse> search(@RequestParam(required = false) Long departmentId,
                                       @RequestParam(required = false) String q) {
        return doctorService.search(departmentId, q);
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @GetMapping("/{id}/availability")
    public List<AvailabilityResponse> availability(@PathVariable Long id) {
        return availabilityService.forDoctor(id);
    }

    @GetMapping("/{id}/slots")
    public List<SlotResponse> slots(@PathVariable Long id,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availabilityService.freeSlots(id, date);
    }
}
