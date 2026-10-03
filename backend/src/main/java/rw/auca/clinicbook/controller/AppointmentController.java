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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST')")
    public AppointmentResponse book(@AuthenticationPrincipal AuthUser user,
                                    @Valid @RequestBody BookAppointmentRequest request) {
        return appointmentService.book(user, request);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> mine(@AuthenticationPrincipal AuthUser user) {
        return appointmentService.myAppointments(user);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public List<AppointmentResponse> forDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.forDay(date);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST','ADMIN')")
    public AppointmentResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return appointmentService.cancel(user, id);
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST','ADMIN')")
    public AppointmentResponse reschedule(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                          @Valid @RequestBody RescheduleRequest request) {
        return appointmentService.reschedule(user, id, request);
    }

    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasRole('RECEPTIONIST')")
    public AppointmentResponse checkIn(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return appointmentService.checkIn(user, id);
    }

    @GetMapping("/{id}/note")
    public VisitNoteResponse note(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return appointmentService.getNote(user, id);
    }
}
