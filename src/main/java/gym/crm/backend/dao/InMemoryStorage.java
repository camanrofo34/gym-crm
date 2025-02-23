package gym.crm.backend.dao;

import gym.crm.backend.domain.Trainee;
import gym.crm.backend.domain.Trainer;
import gym.crm.backend.domain.Training;
import gym.crm.backend.domain.TrainingType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class InMemoryStorage{
    private final Map<String, Map<Long, Object>> storage = new HashMap<>();

    private static final Logger logger = Logger.getLogger(InMemoryStorage.class.getName());

    @Value("${data.file.path}")
    private String dataFilePath;

    public InMemoryStorage() {
        storage.put("Trainer", new HashMap<>());
        storage.put("Trainee", new HashMap<>());
        storage.put("Training", new HashMap<>());
    }

    public void save(String entityName, Long id, Object entity) {
        Map<Long, Object> entities = storage.get(entityName);
        entities.put(id, entity);
    }

    public Object find(String entityName, long id) {
        return storage.get(entityName).get(id);
    }

    public Collection<Object> findAll(String entityName) {
        return storage.get(entityName).values();
    }

    public void delete(String entityName, long id) {
        storage.get(entityName).remove(id);
    }

    public void update(String entityName, long id, Object entity) {
        storage.get(entityName).put(id, entity);
    }

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "Initializing storage from file: {0}", dataFilePath);
        File file= new File(dataFilePath);
        try (FileReader fr = new FileReader(file);
             BufferedReader reader = new BufferedReader(fr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String type = parts[0];
                long id = Long.parseLong(parts[1]);

                switch (type) {
                    case "Trainer" -> {
                        Trainer trainer = new Trainer();
                        trainer.setTrainerId(id);
                        trainer.setFirstName(parts[2]);
                        trainer.setLastName(parts[3]);
                        TrainingType trainingType = new TrainingType();
                        trainingType.setTrainingTypeName(parts[4]);
                        trainer.setSpecialization(trainingType);
                        storage.get("Trainer").put(id, trainer);
                    }
                    case "Trainee" -> {
                        Trainee trainee = new Trainee();
                        trainee.setTraineeId(id);
                        trainee.setFirstName(parts[2]);
                        trainee.setLastName(parts[3]);
                        trainee.setDateOfBirth(parts[4]);
                        trainee.setAddress(parts[5]);
                        storage.get("Trainee").put(id, trainee);
                    }
                    case "Training" -> {
                        Training training = new Training();
                        training.setTrainingId(id);
                        training.setTrainingName(parts[2]);
                        training.setTrainingDate(parts[3]);
                        training.setTrainingDuration(Double.parseDouble(parts[4]));
                        storage.get("Training").put(id, training);
                    }
                }
            }
            logger.log(Level.INFO, "Storage initialized from file: {0}", dataFilePath);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing storage from file: {0}", e.getMessage());
        }
    }
}
