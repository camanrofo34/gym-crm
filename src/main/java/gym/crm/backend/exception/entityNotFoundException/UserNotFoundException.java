package gym.crm.backend.exception.entityNotFoundException;

import jakarta.persistence.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
