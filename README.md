# gym_spring
# Gym Management System 🏋️‍♂️

A **Spring-based** Gym Management System that allows **trainees**, **trainers**, and **trainings** to be managed using a **storage-backed DAO approach**.

## Features 🚀
- **User Management:** Create and manage trainees and trainers.
- **Training Management:** Assign trainees to trainers and manage training sessions.
- **Data Persistence:** Uses **in-memory storage (Map) with JSON file loading**.
- **Spring Components:**
  - **DAO Layer**: Data Access Objects for managing entities.
  - **Service Layer**: Business logic and validation.
  - **Facade Layer**: Simplifies interactions between services.
  - **Logging**: Integrated using `@Slf4j`.
  - **Unit Testing**: JUnit and Mockito-based tests.

## Tech Stack 🛠
- **Java 17**  
- **Spring (non-Boot)**  
- **JUnit 5** & **Mockito**  
- **Lombok** (for reducing boilerplate code)  
- **Jackson** (for JSON processing)  


