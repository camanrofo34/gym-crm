package gym.crm.backend.dao;

import gym.crm.backend.domain.Trainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.logging.Level;
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
        logger.log(Level.INFO, "Creating a new trainer: {0} {1}", new Object[]{trainer.getFirstName(), trainer.getLastName()});
        storage.save("Trainer", trainer.getTrainerId(), trainer);
    }

    public Trainer findTrainer(long id) {
        logger.log(Level.INFO, "Finding trainer with id: {0}", id);
        return (Trainer) storage.find("Trainer", id);
    }

    public Collection<Trainer> findAllTrainers() {
        logger.log(Level.INFO, "Finding all trainers");
        return (Collection<Trainer>)(Collection<?>) storage.findAll("Trainer");
    }

    public void updateTrainer(Trainer trainer) {
        logger.log(Level.INFO, "Updating trainer with id: {0}", trainer.getTrainerId());
        storage.update("Trainer", trainer.getTrainerId(), trainer);
    }

    public void deleteTrainer(long id) {
        logger.log(Level.INFO, "Deleting trainer with id: {0}", id);
        storage.delete("Trainer", id);
    }
}
