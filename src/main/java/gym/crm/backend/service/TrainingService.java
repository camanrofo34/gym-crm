package gym.crm.backend.service;

import gym.crm.backend.dao.TrainingDAO;
import gym.crm.backend.domain.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TrainingService {

    @Autowired
    private TrainingDAO trainingDAO;

    private static final Logger logger = Logger.getLogger(TrainingService.class.getName());

    public void saveTraining(Training training) {
        logger.log(Level.INFO, "Creating a new training: {0} in TrainingService.", training.getTrainingName());
        trainingDAO.saveTraining(training);
    }

    public Training findTraining(long id) {
        logger.log(Level.INFO, "Finding training with id: {0} in TrainingService.", id);
        return trainingDAO.findTraining(id);
    }
}
