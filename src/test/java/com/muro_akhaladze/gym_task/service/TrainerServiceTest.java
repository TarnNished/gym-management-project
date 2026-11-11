package com.muro_akhaladze.gym_task.service;

import com.muro_akhaladze.gym_task.entity.Trainer;
import com.muro_akhaladze.gym_task.exceptions.ApiException;
import com.muro_akhaladze.gym_task.repository.TrainerRepo;
import com.muro_akhaladze.gym_task.repository.TrainingRepo;
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
import org.springframework.data.domain.PageRequest;
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
    @Mock TrainingRepo trainingRepo;
    @Mock FileStorageService fileStorageService;
    @Spy EntityMapper mapper;
    @InjectMocks TrainerService trainerService;

    private Pageable captureActivePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainerRepo).findByUserIsActiveTrue(captor.capture());
        return captor.getValue();
    }

    @Test
    void publicSortKeyIsMappedToEntityPath() {
        when(trainerRepo.findByUserIsActiveTrue(any())).thenReturn(Page.<Trainer>empty());

        trainerService.listTrainers(null, PageRequest.of(2, 5, Sort.by(Sort.Direction.DESC, "lastName")));

        Pageable p = captureActivePageable();
        assertThat(p.getPageNumber()).isEqualTo(2);
        assertThat(p.getPageSize()).isEqualTo(5);
        assertThat(p.getSort().getOrderFor("user.lastName").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(p.getSort().getOrderFor("id")).isNotNull(); // stable tie-breaker
    }

    @Test
    void unsortedRequestDefaultsToLastName() {
        when(trainerRepo.findByUserIsActiveTrue(any())).thenReturn(Page.<Trainer>empty());

        trainerService.listTrainers(null, PageRequest.of(0, 10));

        assertThat(captureActivePageable().getSort().getOrderFor("user.lastName")).isNotNull();
    }

    @Test
    void specializationFilterUsesMethodNameQuery() {
        when(trainerRepo.findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(eq("Yoga"), any()))
                .thenReturn(Page.<Trainer>empty());

        trainerService.listTrainers("Yoga", PageRequest.of(0, 10, Sort.by("firstName")));

        verify(trainerRepo).findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(eq("Yoga"), any());
        verify(trainerRepo, never()).findByUserIsActiveTrue(any());
    }

    @Test
    void pageSizeIsCapped() {
        when(trainerRepo.findByUserIsActiveTrue(any())).thenReturn(Page.<Trainer>empty());
        trainerService.listTrainers(null, PageRequest.of(0, 10_000));
        assertThat(captureActivePageable().getPageSize()).isEqualTo(100);
    }

    @Test
    void unknownOrSensitiveSortKeyIsRejected() {
        assertThatThrownBy(() -> trainerService.listTrainers(null, PageRequest.of(0, 10, Sort.by("password"))))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> trainerService.listTrainers(null, PageRequest.of(0, 10, Sort.by("user.password"))))
                .isInstanceOf(ApiException.class);
        verifyNoInteractions(trainerRepo);
    }
}
