package gym.crm.backend.exception.types.notFound;

import jakarta.persistence.EntityNotFoundException;

public class TrainingTypeNotFoundException extends EntityNotFoundException {

    public TrainingTypeNotFoundException(String message) {
        super(message);
    }
}
