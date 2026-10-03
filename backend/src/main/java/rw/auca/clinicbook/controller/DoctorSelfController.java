package rw.auca.clinicbook.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.security.AuthUser;
import rw.auca.clinicbook.service.AppointmentService;
import rw.auca.clinicbook.service.AvailabilityService;

import java.time.LocalDate;
import java.util.List;

/** The logged-in doctor's own data. */
@RestController
@RequestMapping("/api/v1/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorSelfController {

    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;

    @GetMapping("/availability")
    public List<AvailabilityResponse> myAvailability(@AuthenticationPrincipal AuthUser user) {
        return availabilityService.mine(user);
    }

    @PostMapping("/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityResponse addAvailability(@AuthenticationPrincipal AuthUser user,
                                                @Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.add(user, request);
    }

    @DeleteMapping("/availability/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        availabilityService.delete(user, id);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> schedule(@AuthenticationPrincipal AuthUser user,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.doctorSchedule(user, date);
    }

    @PostMapping("/appointments/{id}/notes")
    public VisitNoteResponse saveNote(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                      @Valid @RequestBody VisitNoteRequest request) {
        return appointmentService.saveNote(user, id, request);
    }
}
