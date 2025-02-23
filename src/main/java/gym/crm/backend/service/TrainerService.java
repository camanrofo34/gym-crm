package gym.crm.backend.service;

import gym.crm.backend.dao.TrainerDAO;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TrainerService {

    @Autowired
    private TrainerDAO trainerDAO;

    private static final Logger logger = Logger.getLogger(TrainerService.class.getName());

    public void saveTrainer(Trainer trainer) {
        logger.log(Level.INFO, "Creating a new trainer: {0} {1}", new Object[]{trainer.getFirstName(), trainer.getLastName()});
        List<String> existingUsernames = trainerDAO.findAllTrainers().stream().map(Trainer::getUsername).toList();
        String username = UserUtil.generateUsername(trainer.getFirstName(), trainer.getLastName(), existingUsernames);
        trainer.setUsername(username);
        String password = UserUtil.generatePassword();
        trainer.setPassword(password);
        trainerDAO.saveTrainer(trainer);
    }

    public Trainer findTrainer(long id) {
        logger.log(Level.INFO, "Finding trainer with id: {0}", id);
        return trainerDAO.findTrainer(id);
    }

    public void updateTrainer(Trainer trainer) {
        logger.log(Level.INFO, "Updating trainer with id: {0}", trainer.getTrainerId());
        trainerDAO.updateTrainer(trainer);
    }

    public void deleteTrainer(long id) {
        logger.log(Level.INFO, "Deleting trainer with id: {0}", id);
        trainerDAO.deleteTrainer(id);
    }

    public Collection<Trainer> findAllTrainers() {
        logger.log(Level.INFO, "Finding all trainers");
        return trainerDAO.findAllTrainers();
    }
}
