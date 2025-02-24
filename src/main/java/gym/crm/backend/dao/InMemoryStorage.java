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

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "Initializing storage from file: {0}", dataFilePath);
        File file = new File(dataFilePath);

        try (FileReader fr = new FileReader(file);
             BufferedReader reader = new BufferedReader(fr)) {

            storage.put("Trainer", new HashMap<>());
            storage.put("Trainee", new HashMap<>());
            storage.put("Training", new HashMap<>());
            storage.put("TrainingType", new HashMap<>());

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 0) continue; // Evita procesar líneas vacías

                String type = parts[0];
                long id = Long.parseLong(parts[1]);

                switch (type) {
                    case "Trainer" -> {
                        // Trainer,ID,FirstName,LastName,Specialization,Username,Password
                        Trainer trainer = new Trainer(id, new TrainingType(parts[4]));
                        trainer.setFirstName(parts[2]);
                        trainer.setLastName(parts[3]);
                        trainer.setUsername(parts[5]);
                        trainer.setPassword(parts[6]);
                        storage.get("Trainer").put(id, trainer);
                    }
                    case "Trainee" -> {
                        // Trainee,ID,FirstName,LastName,DateOfBirth,Address,Username,Password
                        Trainee trainee = new Trainee(id, parts[4], parts[5]);
                        trainee.setFirstName(parts[2]);
                        trainee.setLastName(parts[3]);
                        trainee.setUsername(parts[6]);
                        trainee.setPassword(parts[7]);
                        storage.get("Trainee").put(id, trainee);
                    }
                    case "Training" -> {
                        // Training,ID,TraineeID,TrainerID,TrainingName,TrainingDate,TrainingDuration
                        long traineeId = Long.parseLong(parts[2]);
                        long trainerId = Long.parseLong(parts[3]);
                        Training training = new Training(id, traineeId, trainerId, parts[4], new TrainingType(parts[5]),  parts[6], Double.parseDouble(parts[7]));
                        storage.get("Training").put(id, training);
                    }
                    case "TrainingType" -> {
                        // TrainingType,ID,TrainingTypeName
                        TrainingType trainingType = new TrainingType(parts[2]);
                        storage.get("TrainingType").put(id, trainingType);
                    }
                    default -> logger.log(Level.WARNING, "Unknown record type: {0}", type);
                }
            }
            logger.log(Level.INFO, "Storage successfully initialized from file: {0}", dataFilePath);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing storage from file: {0}", e.getMessage());
        }
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

}
