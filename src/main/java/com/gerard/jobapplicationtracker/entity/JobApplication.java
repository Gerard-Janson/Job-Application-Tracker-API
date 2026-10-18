package com.gerard.jobapplicationtracker.entity;

import com.gerard.jobapplicationtracker.enums.Status;
import com.gerard.jobapplicationtracker.enums.Work_mode;
import com.gerard.jobapplicationtracker.enums.Job_type;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobApplication {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String position;

    @Column(length = 500,nullable = false)
    private String company;

    private String job_description;

    @Column(length = 500)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Job_type job_type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private Work_mode work_mode;

    private BigDecimal salary;

    @Column(length = 500)
    private String post_url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "applied_date")
    private LocalDate appliedDate;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;


}
