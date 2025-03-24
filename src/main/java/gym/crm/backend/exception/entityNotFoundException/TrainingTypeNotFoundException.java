package gym.crm.backend.exception.entityNotFoundException;

import jakarta.persistence.EntityNotFoundException;

public class TrainingTypeNotFoundException extends EntityNotFoundException {

    public TrainingTypeNotFoundException(String message) {
        super(message);
    }
}
