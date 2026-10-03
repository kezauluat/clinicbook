package rw.auca.clinicbook.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "availability",
        indexes = @Index(name = "ix_availability_doctor_day", columnList = "doctor_id, day_of_week"),
        check = @CheckConstraint(name = "ck_availability_time",
                constraint = "start_time < end_time AND day_of_week BETWEEN 1 AND 7"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /** 1 = Monday ... 7 = Sunday */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
