package com.muro_akhaladze.gym_task.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraineeShortInfo {
        private String username;
        private String firstName;
        private String lastName;
}