package rw.auca.clinicbook.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments",
        uniqueConstraints = @UniqueConstraint(name = "uk_department_clinic_name", columnNames = {"clinic_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(nullable = false, length = 80)
    private String name;
}
