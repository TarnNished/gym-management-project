# Gym Management API

A Spring Boot REST backend for a gym. It manages **trainees**, **trainers**, **training types** and **trainings**. Features include JWT login, validation, server-side pagination and sorting, and profile photo upload.

**Stack:** Java 17 · Spring Boot 3.4 · Spring Data JPA (Hibernate) · Spring Security + JWT · H2 (default) or PostgreSQL · Swagger UI

## Run it

You need JDK 17+ and Maven. No database setup is needed: the default profile uses an in-memory H2 database and loads demo data on startup.

```bash
mvn spring-boot:run
```

| What | URL |
|---|---|
| Swagger UI (try every endpoint; log in, then paste the token into **Authorize**) | http://localhost:8080/swagger-ui.html |
| Photo upload demo page (HTML form) | http://localhost:8080/upload.html |
| H2 console (JDBC URL `jdbc:h2:mem:gym`, user `sa`, no password) | http://localhost:8080/h2-console |

**Demo login:** every demo user has the password `password123`, for example `Liam.Anderson` (trainee) or `Emma.Davis` (trainer).

Run the tests (40 tests: HTTP integration, edge cases, repository and unit tests):

```bash
mvn test
```

### PostgreSQL instead of H2

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile reads `DB_URL` (default `jdbc:postgresql://localhost:5432/gym`), `DB_USERNAME` and `DB_PASSWORD`. Set `JWT_SECRET` (at least 32 characters) so login tokens survive a restart. If it is not set, a random secret is generated at startup. Optionally set `UPLOAD_DIR` (default `./uploads`).

## Data model

```
 User 1───1 Trainee  *───*  Trainer *───1 TrainingType
               1               1              1
               │               │              │
               *               *              *
               └──────── Training ────────────┘
```

| Entity | Relationships |
|---|---|
| `User` | Login account: username, BCrypt password, active flag, `photoPath` |
| `Trainee` | `@OneToOne` User · **`@ManyToMany` Trainer** (owning side, join table `trainer_trainee`) |
| `Trainer` | `@OneToOne` User · **`@ManyToOne` TrainingType** (specialization) · `@ManyToMany(mappedBy)` Trainee |
| `Training` | **`@ManyToOne`** Trainee, Trainer, TrainingType |
| `TrainingType` | Lookup table (Yoga, Cardio, ...) |

## Project structure

```
src/main/java/com/muro_akhaladze/gym_task
├── controller/     @RestController  – HTTP layer only (Trainee, Trainer, Training, TrainingType, User, Auth)
├── service/        @Service         – business logic, @Transactional
├── repository/     @Repository      – Spring Data JpaRepository interfaces
├── entity/         @Entity          – JPA model
├── dto/                             – request (validated) / response objects
├── config/         @Configuration   – Security, Web (paging JSON format)
├── security/      @Service / @Component – JWT filter/service, user details, login lockout, access guard
├── exceptions/    @RestControllerAdvice – consistent JSON errors
├── monitoring/    @Component       – Actuator health indicators and metrics
├── logging/       @Component       – request logging, transaction id
└── data/          @Component       – demo-data loader (data.json), uses services only
```

## Main endpoints

All endpoints are under `/api/v1`. **Public** means no token is needed; everything else needs `Authorization: Bearer <token>`.

| Method | Path | Notes |
|---|---|---|
| POST | `/trainees`, `/trainers` | **Public.** Register; returns the generated username and password |
| POST | `/auth/login` | **Public.** Returns a JWT |
| PUT | `/auth/change-password` | |
| GET | `/trainers?specialization=&page=0&size=10&sortBy=lastName&direction=asc` | **Pagination and sorting** |
| GET | `/trainees/{username}/trainings?fromDate=&toDate=&trainerName=&trainingTypeName=&page=0&size=10&sort=trainingDate,desc` | Filters + `Pageable`. Sort keys: `trainingDate`, `trainingName`, `trainingDuration`, `trainingType` |
| GET | `/trainers/{username}/trainings?...` | Same, for a trainer |
| GET / PUT / DELETE | `/trainees/{username}` | Profile; delete cascades to trainings, links and photo |
| GET / PUT / DELETE | `/trainers/{username}` | Profile; delete cascades to trainings, links and photo |
| PATCH | `/trainees/{username}`, `/trainers/{username}` | Activate or deactivate |
| PUT | `/trainees/{username}/trainers` | **Many-to-many:** set the trainee's trainers |
| GET | `/trainees/{username}/unassigned-trainers` | |
| POST | `/trainings` | Add a training (caller must be the trainee or trainer, both active; also links them) |
| DELETE | `/trainings/{id}` | Only the trainee or trainer of that training |
| GET | `/training-types` | **Public** |
| POST | `/users/{username}/photo` | **File upload** (`multipart/form-data`, field `file`). JPEG/PNG/WEBP/GIF only, checked by the file's first bytes, max 5 MB |
| GET | `/users/{username}/photo` | Download a photo (any logged-in user) |
| DELETE | `/users/{username}/photo` | |

A user can only read or change their own profile; anything else returns 403. All errors share one JSON shape: `{"timestamp", "error", "message", "fieldErrors"?}`. After 3 wrong passwords, that username is blocked for 5 minutes from that IP address only.

### Quick curl tour

```bash
# log in
TOKEN=$(curl -s -H "Content-Type: application/json" \
  -d '{"username":"Liam.Anderson","password":"password123"}' \
  http://localhost:8080/api/v1/auth/login | sed -E 's/.*"token":"([^"]+)".*/\1/')

# page 2 of trainers, 3 per page, sorted by last name descending
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/trainers?page=1&size=3&sortBy=lastName&direction=desc"

# upload a profile photo
curl -H "Authorization: Bearer $TOKEN" -F "file=@photo.png" \
  http://localhost:8080/api/v1/users/Liam.Anderson/photo

# assign trainers (many-to-many)
curl -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"trainerUsernames":["Emma.Davis","Oliver.Martinez"]}' \
  http://localhost:8080/api/v1/trainees/Liam.Anderson/trainers
```

## Assignment requirements: where to find them

| Requirement | Where |
|---|---|
| Layered packages | `controller`, `service`, `repository`, `entity` (+ `dto`, `config`) |
| Stereotype annotations | Every Spring bean class has one: `@RestController`, `@Service`, `@Repository`, `@Configuration`, `@Component`, `@RestControllerAdvice` (entities have `@Entity`; DTOs are plain data classes) |
| Constructor injection, no field `@Autowired` | All beans use constructors (explicit, or Lombok `@RequiredArgsConstructor`). There is no `@Autowired` anywhere, not even in tests (they use `@TestConstructor`) |
| ≥ 3 entities | `User`, `Trainee`, `Trainer`, `Training`, `TrainingType` |
| ≥ 2 relationship types | `@OneToOne`, `@ManyToOne`, `@ManyToMany` (`Trainee.trainers` ↔ `Trainer.trainees`) |
| Repositories extend Spring Data | All five extend `JpaRepository` |
| ≥ 2 method-name queries | `TrainerRepo.findBySpecializationTrainingTypeNameIgnoreCaseAndUserIsActiveTrue`, `findByUserIsActiveTrue`, `findByUserUserNameIn`, `findByUserUserName`; `TraineeRepo.findByUserUserName`; `TrainingTypeRepo.findByTrainingTypeNameIgnoreCase`; `UserRepo.existsByUserName` |
| ≥ 2 custom `@Query` | `TrainingRepo.findTraineeTrainings` / `findTrainerTrainings` (filters + paging), `deleteAllByTrainee` (`@Modifying`); `TrainerRepo.findTrainersNotAssignedToTrainee` (sub-select), `findByTraineeUsername`; `TraineeRepo.findByTrainerUsername` |
| `@Transactional` on data changes | Every create/update/delete in `TraineeService`, `TrainerService`, `TrainingService`, `TrainingTypeService`, `UserService` (reads use `readOnly = true`) |
| `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` | `TraineeController`, `TrainerController`, `UserController` |
| `@PathVariable` | `/{username}` on every profile endpoint |
| `@RequestParam` | `TrainerController.listTrainers` (`specialization`, `page`, `size`, `sortBy`, `direction`); `UserController.uploadPhoto` (`file`) |
| `@RequestBody` / `@ModelAttribute` | JSON bodies everywhere; `@ModelAttribute` for the trainings filter objects |
| JSR-303 validation + `@Valid` | `dto/**/*Request` (`@NotBlank`, `@NotNull`, `@Size`, `@Past`, `@Positive`, `@Max`, `@AssertTrue`); every `@RequestBody` and `@ModelAttribute` is `@Valid` |
| File upload (`MultipartFile`, `multipart/form-data`) | `UserController.uploadPhoto` → `FileStorageService` saves to `uploads/`, file name stored in `users.photo_path`; HTML form in `static/upload.html` |
| Pagination and sorting (`Pageable`, `Sort`) | `TrainerService.listTrainers` builds `PageRequest.of(page, size, Sort.by(...))`; trainings endpoints take a `Pageable` (`@PageableDefault`). Sort keys are checked against an allowed list in both places (unknown keys return 400) |
