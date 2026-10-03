package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.entity.Clinic;
import rw.auca.clinicbook.entity.Department;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.ClinicRepository;
import rw.auca.clinicbook.repository.DepartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final DepartmentRepository departmentRepository;

    @Cacheable("clinics")
    @Transactional(readOnly = true)
    public List<ClinicResponse> listClinics() {
        return clinicRepository.findAll(Sort.by("name")).stream().map(ClinicResponse::from).toList();
    }

    @Cacheable("departments")
    @Transactional(readOnly = true)
    public List<DepartmentResponse> listDepartments() {
        return departmentRepository.findAll(Sort.by("name")).stream().map(DepartmentResponse::from).toList();
    }

    @CacheEvict(cacheNames = "clinics", allEntries = true)
    @Transactional
    public ClinicResponse createClinic(ClinicRequest req) {
        if (clinicRepository.existsByName(req.name().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "A clinic with this name already exists");
        }
        Clinic clinic = clinicRepository.save(Clinic.builder()
                .name(req.name().trim()).address(req.address()).phone(req.phone()).build());
        return ClinicResponse.from(clinic);
    }

    @CacheEvict(cacheNames = "departments", allEntries = true)
    @Transactional
    public DepartmentResponse createDepartment(Long clinicId, DepartmentRequest req) {
        Clinic clinic = clinicRepository.findById(clinicId).orElseThrow(() -> ApiException.notFound("Clinic"));
        Department dep = departmentRepository.save(Department.builder().clinic(clinic).name(req.name().trim()).build());
        return DepartmentResponse.from(dep);
    }
}
