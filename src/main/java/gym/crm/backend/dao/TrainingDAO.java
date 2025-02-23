package gym.crm.backend.dao;

import gym.crm.backend.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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
        logger.info("Creating a new training: " + training.getTrainingName() + " in TrainingDAO.");
        storage.save("Training", training.getTrainingId(), training);
    }

    public Training findTraining(long id) {
        logger.info("Finding training with id: " + id + " in TrainingDAO.");
        return (Training) storage.find("Training", id);
    }

    public void deleteTraining(long id) {
        logger.info("Deleting training with id: " + id + " in TrainingDAO.");
        storage.delete("Training", id);
    }

    public void updateTraining(Training training) {
        logger.info("Updating training with id: " + training.getTrainingId() + " in TrainingDAO.");
        storage.update("Training", training.getTrainingId(), training);
    }

    public Collection<Training> findAllTrainings() {
        logger.info("Finding all trainings" + " in TrainingDAO.");
        return (Collection<Training>)(Collection<?>) storage.findAll("Training");
    }
}
