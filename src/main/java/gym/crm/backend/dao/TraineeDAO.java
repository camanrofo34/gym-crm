package gym.crm.backend.dao;

import gym.crm.backend.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.logging.Logger;

@Repository
public class TraineeDAO {
    private InMemoryStorage storage;
    private static final Logger logger = Logger.getLogger(TraineeDAO.class.getName());

    @Autowired
    public TraineeDAO (InMemoryStorage storage) {
        this.storage = storage;
    }

    public void saveTrainee(Trainee trainee) {
        logger.info("Creating a new trainee: " + trainee.getFirstName() + trainee.getLastName() + " in TraineeDAO.");
        storage.save("Trainee", trainee.getTraineeId(), trainee);
    }

    public Trainee findTrainee(long id) {
        logger.info("Finding trainee with id: " + id + " in TraineeDAO.");
        return (Trainee) storage.find("Trainee", id);
    }

    public void deleteTrainee(long id) {
        logger.info("Deleting trainee with id: " + id + " in TraineeDAO.");
        storage.delete("Trainee", id);
    }

    public void updateTrainee(Trainee trainee) {
        logger.info("Updating trainee with id: " + trainee.getTraineeId() + " in TraineeDAO.");
        storage.update("Trainee", trainee.getTraineeId(), trainee);
    }

    public Collection<Trainee> findAllTrainees() {
        logger.info("Finding all trainees" + " in TraineeDAO.");
        return (Collection<Trainee>)(Collection<?>) storage.findAll("Trainee");
    }
}
