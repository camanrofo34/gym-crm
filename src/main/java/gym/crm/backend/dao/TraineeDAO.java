package gym.crm.backend.dao;

import gym.crm.backend.domain.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.logging.Level;
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
        logger.log(Level.INFO, "Creating a new trainee: {0} {1}", new Object[]{trainee.getFirstName(), trainee.getLastName()});
        storage.save("Trainee", trainee.getTraineeId(), trainee);
    }

    public Trainee findTrainee(long id) {
        logger.log(Level.INFO, "Finding trainee with id: {0}", id);
        return (Trainee) storage.find("Trainee", id);
    }

    public void deleteTrainee(long id) {
        logger.log(Level.INFO, "Deleting trainee with id: {0}", id);
        storage.delete("Trainee", id);

    }

    public void updateTrainee(Trainee trainee) {
        logger.log(Level.INFO, "Updating trainee with id: {0}", trainee.getTraineeId());
        storage.update("Trainee", trainee.getTraineeId(), trainee);
    }

    public Collection<Trainee> findAllTrainees() {
        logger.log(Level.INFO, "Finding all trainees");
        return (Collection<Trainee>)(Collection<?>) storage.findAll("Trainee");
    }
}
