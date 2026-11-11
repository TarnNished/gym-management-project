package com.muro_akhaladze.gym_task.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
    public  class TrainerShortInfo {
        private String username;
        private String firstName;
        private String lastName;
        private String specialization;
    }