package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.TrainingTypeRepo;
import com.muro_akhaladze.gym_task.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock TrainerRepo trainerRepo;
    @Mock TrainingTypeRepo trainingTypeRepo;
    @Mock UserRepo userRepo;
    @Mock UserService userService;
    @Mock com.muro_akhaladze.gym_task.repository.TrainingRepo trainingRepo;
    @Mock FileStorageService fileStorageService;
    @Spy EntityMapper mapper;
    @InjectMocks TrainerService trainerService;

    @Test
    void listTrainersBuildsPageableWithMappedSortProperty() {
        when(trainerRepo.findByUserIsActiveTrue(any())).thenReturn(Page.<Trainer>empty());

        trainerService.listTrainers(null, 2, 5, "lastName", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainerRepo).findByUserIsActiveTrue(captor.capture());
        Pageable p = captor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(2);
        assertThat(p.getPageSize()).isEqualTo(5);
        assertThat(p.getSort().getOrderFor("user.lastName").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void listTrainersUsesSpecializationQueryWhenFilterGiven() {
        when(trainerRepo.findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(eq("Yoga"), any()))
                .thenReturn(Page.<Trainer>empty());

        trainerService.listTrainers("Yoga", 0, 10, "firstName", "asc");

        verify(trainerRepo).findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(eq("Yoga"), any());
        verify(trainerRepo, never()).findByUserIsActiveTrue(any());
    }

    @Test
    void pageSizeIsCapped() {
        when(trainerRepo.findByUserIsActiveTrue(any())).thenReturn(Page.<Trainer>empty());
        trainerService.listTrainers(null, -3, 10_000, "username", "asc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainerRepo).findByUserIsActiveTrue(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void unknownSortFieldOrDirectionIsRejected() {
        assertThatThrownBy(() -> trainerService.listTrainers(null, 0, 10, "password", "asc"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> trainerService.listTrainers(null, 0, 10, "lastName", "sideways"))
                .isInstanceOf(ApiException.class);
        verifyNoInteractions(trainerRepo);
    }
}
