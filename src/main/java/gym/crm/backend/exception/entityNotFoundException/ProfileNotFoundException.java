package gym.crm.backend.exception.entityNotFoundException;

import jakarta.persistence.EntityNotFoundException;

public class ProfileNotFoundException extends EntityNotFoundException {

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
