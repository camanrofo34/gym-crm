package gym.crm.hours_microservice.exception.types.forbidden;

public class InvalidateTokenException extends RuntimeException {

    public InvalidateTokenException(String message) {
        super(message);
    }

}
