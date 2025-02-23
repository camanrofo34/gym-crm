package gym.crm.backend.dao;

import gym.crm.backend.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.logging.Logger;

@Repository
public class TrainerDAO {
    private InMemoryStorage storage;
    private static final Logger logger = Logger.getLogger(TrainerDAO.class.getName());

    @Autowired
    public TrainerDAO(InMemoryStorage storage) {
        this.storage = storage;
    }

    public void saveTrainer(Trainer trainer) {
        logger.info("Creating a new trainer: " + trainer.getFirstName() + trainer.getLastName() + " in TrainerDAO.");
        storage.save("Trainer", trainer.getTrainerId(), trainer);
    }

    public Trainer findTrainer(long id) {
        logger.info("Finding trainer with id: " + id + " in TrainerDAO.");
        return (Trainer) storage.find("Trainer", id);
    }

    public Collection<Trainer> findAllTrainers() {
        logger.info("Finding all trainers" + " in TrainerDAO.");
        return (Collection<Trainer>)(Collection<?>) storage.findAll("Trainer");
    }

    public void updateTrainer(Trainer trainer) {
        logger.info("Updating trainer with id: " + trainer.getTrainerId() + " in TrainerDAO.");
        storage.update("Trainer", trainer.getTrainerId(), trainer);
    }

    public void deleteTrainer(long id) {
        logger.info("Deleting trainer with id: " + id + " in TrainerDAO.");
        storage.delete("Trainer", id);
    }
}
