package gym.crm.hours_microservice.messaging.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessagingExceptionHandler {

    @JmsListener(destination = "ActiveMQ.DLQ")
    public void handleInvalidMessages(String message) {
        log.warn("Received message in DLQ: {}", message);
    }

}
