package gym.crm.backend.service;

import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.User;
import gym.crm.backend.repository.TrainerRepository;
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

        Trainer result = trainerService.createTrainer(trainer);

        assertNotNull(result);
        assertEquals("Jane", result.getUser().getFirstName());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void createTrainer_Failure_InvalidData() {
        Trainer trainer = new Trainer();

        Trainer result = trainerService.createTrainer(trainer);

        assertNull(result);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void getTrainerByUsername_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("janedoe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.getTrainerByUsername("janedoe");

        assertTrue(result.isPresent());
        assertEquals("janedoe", result.get().getUser().getUsername());
    }

    @Test
    void getTrainerByUsername_NotFound() {
        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.empty());

        Optional<Trainer> result = trainerService.getTrainerByUsername("janedoe");

        assertFalse(result.isPresent());
    }

    @Test
    void matchTrainerUsernameAndPassword_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("janedoe");
        user.setPassword("password123");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.of(trainer));

        boolean result = trainerService.matchTrainerUsernameAndPassword("janedoe", "password123");

        assertTrue(result);
    }

    @Test
    void matchTrainerUsernameAndPassword_Failure() {
        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.empty());

        boolean result = trainerService.matchTrainerUsernameAndPassword("janedoe", "password123");

        assertFalse(result);
    }

    @Test
    void changePassword_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("janedoe");
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.of(trainer));

        trainerService.changePassword("janedoe", "newpassword");

        assertEquals("newpassword", trainer.getUser().getPassword());
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Test
    void changePassword_TrainerNotFound() {
        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.empty());

        trainerService.changePassword("janedoe", "newpassword");

        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void activateDeactivateTrainer_Success() {
        Trainer trainer = new Trainer();
        User user = new User();
        user.setUsername("janedoe");
        user.setIsActive(true);
        trainer.setUser(user);

        when(trainerRepository.findByUserUsername("janedoe")).thenReturn(Optional.of(trainer));

        trainerService.activateDeactivateTrainer("janedoe");

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