package gym.crm.backend.exception.types.timeout;

import java.net.SocketTimeoutException;

public class UncheckedSocketTimeoutException extends SocketTimeoutException {
    public UncheckedSocketTimeoutException(String message) {
        super(message);
    }
}

