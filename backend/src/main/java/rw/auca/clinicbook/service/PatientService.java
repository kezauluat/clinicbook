package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.PatientResponse;
import rw.auca.clinicbook.repository.PatientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public List<PatientResponse> search(String q) {
        return patientRepository.search(q == null ? "" : q.trim()).stream().map(PatientResponse::from).toList();
    }
}
