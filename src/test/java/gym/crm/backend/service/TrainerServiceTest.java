package gym.crm.backend.service;

import gym.crm.backend.dao.TrainerDAO;
import gym.crm.backend.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTrainerCreatesNewTrainer() {
        Trainer trainer = new Trainer("Alice", "Johnson", "Weightlifting");

        trainerService.saveTrainer(trainer);

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerDAO).saveTrainer(captor.capture());

        Trainer savedTrainer = captor.getValue();
        assertNotNull(savedTrainer.getUsername());
        assertNotNull(savedTrainer.getPassword());
    }

    @Test
    void findTrainerReturnsTrainer() {
        Trainer trainer = new Trainer(3L, "Bob", "Brown", "Yoga");

        when(trainerDAO.findTrainer(3L)).thenReturn(trainer);

        Trainer foundTrainer = trainerService.findTrainer(3L);

        assertEquals(trainer, foundTrainer);
    }

    @Test
    void deleteTrainerRemovesTrainer() {
        Trainer trainer = new Trainer(4L, "Charlie", "Davis", "Cardio");

        when(trainerDAO.findTrainer(4L)).thenReturn(trainer);

        trainerService.deleteTrainer(4L);

        verify(trainerDAO).deleteTrainer(4L);
    }

    @Test
    void updateTrainerUpdatesExistingTrainer() {
        Trainer trainer = new Trainer(5L, "David", "Evans", "Pilates");

        when(trainerDAO.findTrainer(5)).thenReturn(trainer);

        trainer.setLastName("Smith");

        trainerService.updateTrainer(trainer);

        verify(trainerDAO).updateTrainer(any(Trainer.class));
        assertEquals("Smith", trainer.getLastName());
    }

    @Test
    void findAllTrainersReturnsAllTrainers() {
        Trainer trainer1 = new Trainer("Eve", "Foster", "Zumba");
        Trainer trainer2 = new Trainer("Frank", "Green", "CrossFit");

        when(trainerDAO.findAllTrainers()).thenReturn(List.of(trainer1, trainer2));

        Collection<Trainer> trainers = trainerService.findAllTrainers();

        assertEquals(2, trainers.size());
        assertTrue(trainers.contains(trainer1));
        assertTrue(trainers.contains(trainer2));
    }
}