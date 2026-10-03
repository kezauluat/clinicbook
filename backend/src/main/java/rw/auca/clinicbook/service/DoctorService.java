package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.DoctorResponse;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.DoctorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /** Cached in Redis for 10 minutes; cleared when an admin adds a doctor. */
    @Cacheable(cacheNames = "doctors", key = "(#departmentId == null ? 'all' : #departmentId) + ':' + (#q == null ? '' : #q.trim().toLowerCase())")
    @Transactional(readOnly = true)
    public List<DoctorResponse> search(Long departmentId, String q) {
        String term = q == null ? "" : q.trim();
        return doctorRepository.search(departmentId, term).stream().map(DoctorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse get(Long id) {
        return doctorRepository.findById(id).map(DoctorResponse::from)
                .orElseThrow(() -> ApiException.notFound("Doctor"));
    }
}
