package gym.crm.backend.dao;

import gym.crm.backend.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class TrainingDAO {
    private InMemoryStorage storage;
    private static final Logger logger = Logger.getLogger(TrainingDAO.class.getName());

    @Autowired
    public TrainingDAO(InMemoryStorage storage) {
        this.storage = storage;
    }

    public void saveTraining(Training training) {
        logger.log(Level.INFO, "Creating a new training: {0} in TrainingDAO.", training.getTrainingName());
        storage.save("Training", training.getTrainingId(), training);
    }

    public Training findTraining(long id) {
        logger.log(Level.INFO, "Finding training with id: {0} in TrainingDAO.", id);
        return (Training) storage.find("Training", id);
    }

    public void deleteTraining(long id) {
        logger.log(Level.INFO, "Deleting training with id: {0} in TrainingDAO.", id);
        storage.delete("Training", id);
    }

    public void updateTraining(Training training) {
        logger.log(Level.INFO, "Updating training with id: {0} in TrainingDAO.", training.getTrainingId());
        storage.update("Training", training.getTrainingId(), training);
    }

    public Collection<Training> findAllTrainings() {
        logger.log(Level.INFO, "Finding all trainings in TrainingDAO.");
        return (Collection<Training>)(Collection<?>) storage.findAll("Training");
    }
}
