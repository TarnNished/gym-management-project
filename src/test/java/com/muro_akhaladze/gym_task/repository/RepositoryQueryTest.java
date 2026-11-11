package com.muro_akhaladze.gym_task.repository;

import com.muro_akhaladze.gym_task.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestConstructor;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests the method-name queries and the custom @Query methods against H2. */
@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RepositoryQueryTest {

    private final TestEntityManager em;
    private final TraineeRepo traineeRepo;
    private final TrainerRepo trainerRepo;
    private final TrainingRepo trainingRepo;
    private final TrainingTypeRepo trainingTypeRepo;
    private final UserRepo userRepo;

    // constructor injection (enabled by @TestConstructor)
    RepositoryQueryTest(TestEntityManager em, TraineeRepo traineeRepo, TrainerRepo trainerRepo,
                        TrainingRepo trainingRepo, TrainingTypeRepo trainingTypeRepo, UserRepo userRepo) {
        this.em = em;
        this.traineeRepo = traineeRepo;
        this.trainerRepo = trainerRepo;
        this.trainingRepo = trainingRepo;
        this.trainingTypeRepo = trainingTypeRepo;
        this.userRepo = userRepo;
    }

    Trainee liam;
    Trainer oliver;
    Trainer sophia;
    Trainer inactive;

    private User user(String first, String last, boolean active) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        u.setUserName(first + "." + last);
        u.setPassword("hash");
        u.setActive(active);
        return em.persist(u);
    }

    @BeforeEach
    void setUp() {
        TrainingType yoga = em.persist(new TrainingType("Yoga"));
        TrainingType cardio = em.persist(new TrainingType("Cardio"));

        oliver = em.persist(new Trainer(user("Oliver", "Martinez", true), yoga));
        sophia = em.persist(new Trainer(user("Sophia", "Taylor", true), cardio));
        inactive = em.persist(new Trainer(user("Old", "Coach", false), yoga));

        liam = new Trainee(user("Liam", "Anderson", true), LocalDate.of(1996, 6, 15), "Oak St");
        liam.addTrainer(oliver);
        em.persist(liam);

        Training t = new Training();
        t.setTrainee(liam);
        t.setTrainer(oliver);
        t.setTrainingType(yoga);
        t.setTrainingName("Stretch");
        t.setTrainingDate(LocalDate.of(2025, 3, 10));
        t.setTrainingDuration(60L);
        em.persist(t);
        em.flush();
        em.clear();
    }

    @Test
    void methodNameQueries() {
        assertThat(userRepo.existsByUserName("Liam.Anderson")).isTrue();
        assertThat(traineeRepo.findByUserUserName("Liam.Anderson")).isPresent();
        assertThat(trainingTypeRepo.findByTrainingTypeNameIgnoreCase("yOgA")).isPresent();
        assertThat(trainerRepo.findByUserUserNameIn(List.of("Oliver.Martinez", "Sophia.Taylor"))).hasSize(2);

        Page<Trainer> activeYoga = trainerRepo.findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue(
                "yoga", PageRequest.of(0, 10));
        assertThat(activeYoga.getContent()).extracting(tr -> tr.getUser().getUsername())
                .containsExactly("Oliver.Martinez");

        Page<Trainer> active = trainerRepo.findByUserIsActiveTrue(PageRequest.of(0, 1, Sort.by("user.lastName")));
        assertThat(active.getTotalElements()).isEqualTo(2);
        assertThat(active.getContent().get(0).getUser().getLastName()).isEqualTo("Martinez");
    }

    @Test
    void customQueriesOverManyToMany() {
        assertThat(trainerRepo.findByTraineeUsername("Liam.Anderson"))
                .extracting(tr -> tr.getUser().getUsername()).containsExactly("Oliver.Martinez");
        assertThat(traineeRepo.findByTrainerUsername("Oliver.Martinez"))
                .extracting(t -> t.getUser().getUsername()).containsExactly("Liam.Anderson");
        // inactive trainers are excluded, assigned ones too
        assertThat(trainerRepo.findTrainersNotAssignedToTrainee("Liam.Anderson"))
                .extracting(tr -> tr.getUser().getUsername()).containsExactly("Sophia.Taylor");
    }

    @Test
    void customTrainingQueryWithFilters() {
        PageRequest page = PageRequest.of(0, 10);
        assertThat(trainingRepo.findTraineeTrainings("Liam.Anderson", null, null, null, null, page).getTotalElements())
                .isEqualTo(1);
        assertThat(trainingRepo.findTraineeTrainings("Liam.Anderson", LocalDate.of(2025, 4, 1), null, null, null, page)
                .getTotalElements()).isZero();
        assertThat(trainingRepo.findTrainerTrainings("Oliver.Martinez", null, null, "Liam.Anderson", page)
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteAllByTrainee() {
        Trainee trainee = traineeRepo.findByUserUserName("Liam.Anderson").orElseThrow();
        trainingRepo.deleteAllByTrainee(trainee);
        assertThat(trainingRepo.count()).isZero();
    }
}
