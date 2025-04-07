package gym.crm.backend.service;

import gym.crm.backend.domain.entities.Trainer;
import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.TrainingTypeNotFoundException;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {
@BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}

@InjectMocks
private TrainerService trainerService;

@Mock
private TrainerRepository trainerRepository;

@Mock
private TrainingTypeRepository trainingTypeRepository;

@Mock
private UserRepository userRepository;

@Mock
private UserCredentialService userCredentialService;

@Test
void createTrainer_Success() {
    TrainerCreationRequest request = new TrainerCreationRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setTrainingTypeId(1L);

    when(userCredentialService.generateUsername(anyString(), anyString())).thenReturn("johndoe");
    when(userCredentialService.generatePassword()).thenReturn("password123");
    when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(new TrainingType()));
    when(userRepository.save(any(User.class))).thenReturn(new User());
    when(trainerRepository.save(any(Trainer.class))).thenReturn(new Trainer());

    UserCreationResponse response = trainerService.createTrainer(request);

    assertEquals("johndoe", response.getUsername());
    assertEquals("password123", response.getPassword());
}

@Test
void createTrainer_TrainingTypeNotFound() {
    TrainerCreationRequest request = new TrainerCreationRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setTrainingTypeId(1L);

    when(userCredentialService.generateUsername(anyString(), anyString())).thenReturn("johndoe");
    when(userCredentialService.generatePassword()).thenReturn("password123");
    when(trainingTypeRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TrainingTypeNotFoundException.class, () -> trainerService.createTrainer(request));
}

@Test
void getTrainerByUsername_Success() {
    String username = "johndoe";
    Trainer trainer = new Trainer();
    trainer.setTrainees(new HashSet<>());
    trainer.setSpecialization(new TrainingType());
    User user = new User();
    user.setFirstName("john");
    user.setUsername(username);
    trainer.setUser(user);

    when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));

    TrainerGetProfileResponse response = trainerService.getTrainerByUsername(username);

    assertNotNull(response);
    assertEquals("john", response.getFirstName());
}

@Test
void getTrainerByUsername_NotFound() {
    String username = "unknown";

    when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.empty());

    assertThrows(ProfileNotFoundException.class, () -> trainerService.getTrainerByUsername(username));
}

@Test
void updateTrainerProfile_Success() {
    String username = "johndoe";
    TrainerUpdateRequest request = new TrainerUpdateRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setTrainingTypeId(1L);

    Trainer trainer = new Trainer();
    trainer.setTrainees(new HashSet<>());
    trainer.setSpecialization(new TrainingType());
    User user = new User();
    user.setUsername(username);
    trainer.setUser(user);

    when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));
    when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(new TrainingType()));
    when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

    TrainerUpdateResponse response = trainerService.updateTrainerProfile(username, request);

    assertNotNull(response);
    assertEquals("John", response.getFirstName());
    assertEquals("Doe", response.getLastName());
}

@Test
void updateTrainerProfile_TrainerNotFound() {
    String username = "unknown";
    TrainerUpdateRequest request = new TrainerUpdateRequest();

    when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.empty());

    assertThrows(ProfileNotFoundException.class, () -> trainerService.updateTrainerProfile(username, request));
}

@Test
void updateTrainerProfile_TrainingTypeNotFound() {
    String username = "johndoe";
    TrainerUpdateRequest request = new TrainerUpdateRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setTrainingTypeId(1L);

    Trainer trainer = new Trainer();
    User user = new User();
    user.setUsername(username);
    trainer.setUser(user);

    when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));
    when(trainingTypeRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TrainingTypeNotFoundException.class, () -> trainerService.updateTrainerProfile(username, request));
}
}