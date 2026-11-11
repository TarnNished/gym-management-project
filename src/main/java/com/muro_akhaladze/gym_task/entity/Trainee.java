package com.muro_akhaladze.gym_task.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Trainee {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @OneToOne(cascade = CascadeType.REMOVE)
        @JoinColumn(name = "user_id", nullable = false, unique = true)
        @NotNull(message = "User is required")
        private User user;

        @Column(nullable = false)
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        @Column(nullable = false, length = 50)
        @Size(max = 50, message = "Address cannot exceed 50 characters")
        private String address;



    public Trainee(User user,LocalDate  dateOfBirth, String address) {
            this.user = user;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
        }
    }
