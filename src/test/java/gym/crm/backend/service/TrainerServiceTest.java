package gym.crm.backend.service;

import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.User;
import gym.crm.backend.repository.TrainerRepository;
import gym.crm.backend.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserUtil userUtil;

    @InjectMocks
    private TrainerService trainerService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTrainer_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        trainer.setUser(user);

        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(userUtil.generateUsername(anyString(), anyString(), anyList())).thenReturn("jane.doe");
        when(userUtil.generatePassword()).thenReturn("password123");

        Optional<Trainer> result = trainerService.createTrainer(trainer);

        assertNotNull(result);
        assertEquals("Jane", result.get().getUser().getFirstName());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void createTrainer_Failure_InvalidData() {
        Trainer trainer = new Trainer();

        Optional<Trainer> result = trainerService.createTrainer(trainer);

        assertFalse(result.isPresent());
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void getTrainerByUsername_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("jane.doe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainerByUsername("jane.doe");

        assertTrue(result.isPresent());
        assertEquals("jane.doe", result.get().getUser().getUsername());
    }

    @Test
    void getTrainerByUsername_NotFound() {
        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getTrainerByUsername("jane.doe");

        assertFalse(result.isPresent());
    }

    @Test
    void matchTrainerUsernameAndPassword_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("jane.doe");
        user.setPassword("password123");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.matchTrainerUsernameAndPassword("jane.doe", "password123");

        assertTrue(result);
    }

    @Test
    void matchTrainerUsernameAndPassword_FailureByUsername() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("jane.doe");
        user.setPassword("password123");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.matchTrainerUsernameAndPassword("jane.doe", "wrong");

        assertFalse(result);
    }

    @Test
    void matchTrainerUsernameAndPassword_FailureByPassword() {
        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.empty());

        boolean result = trainerService.matchTrainerUsernameAndPassword("jane.doe", "password123");

        assertFalse(result);
    }

    @Test
    void changePassword_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("jane.doe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        trainerService.changePassword("jane.doe", "newpassword");

        assertEquals("newpassword", trainer.getUser().getPassword());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void changePassword_TrainerNotFound() {
        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.empty());

        trainerService.changePassword("jane.doe", "newpassword");

        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void updateTrainerProfile_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setUsername("jane.doe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        trainerService.updateTrainerProfile(trainer);

        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void updateTrainerProfile_Failure() {
        Trainer trainer = new Trainer();

        trainerService.updateTrainerProfile(trainer);

        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void activateDeactivateTrainer_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("jane.doe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("jane.doe")).thenReturn(Optional.of(trainer));

        trainerService.activateDeactivateTrainer("jane.doe");

        assertFalse(trainer.getUser().getIsActive());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void activateDeactivateTrainer_TrainerNotFound() {
        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.empty());

        trainerService.activateDeactivateTrainer("janedoe");

        verify(trainerRepository, never()).save(any(Trainer.class));
    }
}