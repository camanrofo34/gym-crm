package gym.crm.backend.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${queue.trainer-workload}")
    private String queue;

    @Retryable(
        value = {RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendTrainerWorkloadRequest(TrainerWorkloadRequest request, String transactionId, String bearerToken) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(request);
            jmsTemplate.convertAndSend(queue, jsonPayload, message -> {
                message.setStringProperty("Transaction-Id", transactionId);
                message.setStringProperty("Authorization", bearerToken);
                return message;
            });
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TrainerWorkloadRequest to JSON", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to queue: " + queue, e);
        }
    }

    @Recover
    public void recover(RuntimeException e, TrainerWorkloadRequest request, String transactionId, String bearerToken) {
        log.error("Failed to send TrainerWorkloadRequest after retries: {}", e.getMessage());
    }
}

