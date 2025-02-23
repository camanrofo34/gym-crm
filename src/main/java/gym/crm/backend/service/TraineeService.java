package gym.crm.backend.service;

import gym.crm.backend.dao.TraineeDAO;
import gym.crm.backend.domain.Trainee;
import gym.crm.backend.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TraineeService {

    @Autowired
    private TraineeDAO traineeDAO;

    private static final Logger logger = Logger.getLogger(TraineeService.class.getName());


    public void saveTrainee(Trainee trainee) {
        logger.log(Level.INFO, "Creating a new trainee: {0} {1}", new Object[]{trainee.getFirstName(), trainee.getLastName()});
        List<String> existingUsernames = traineeDAO.findAllTrainees().stream().map(Trainee::getUsername).toList();
        String username = UserUtil.generateUsername(trainee.getFirstName(), trainee.getLastName(), existingUsernames);
        trainee.setUsername(username);
        String password = UserUtil.generatePassword();
        trainee.setPassword(password);
        traineeDAO.saveTrainee(trainee);
    }

    public Trainee findTrainee(long id) {
        logger.log(Level.INFO, "Finding trainee with id: {0}", id);
        return traineeDAO.findTrainee(id);
    }

    public void deleteTrainee(long id) {
        logger.log(Level.INFO, "Deleting trainee with id: {0}", id);
        traineeDAO.deleteTrainee(id);
    }

    public void updateTrainee(Trainee trainee) {
        logger.log(Level.INFO, "Updating trainee with id: {0}", trainee.getTraineeId());
        traineeDAO.updateTrainee(trainee);
    }

    public Collection<Trainee> findAllTrainees() {
        logger.log(Level.INFO, "Finding all trainees");
        return traineeDAO.findAllTrainees();
    }
}
