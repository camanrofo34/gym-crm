package gym.crm.backend.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainerWorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${queue.trainer-workload}")
    private String queue;

    public void sendTrainerWorkloadRequest(TrainerWorkloadRequest request, String transactionId, String bearerToken) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(request);
            jmsTemplate.convertAndSend(queue, jsonPayload, message -> {
                message.setStringProperty("Transaction-Id", transactionId);
                message.setStringProperty("Authorization", bearerToken);
                return message;
            });
        }catch (JsonProcessingException e){
            throw new RuntimeException("Failed to serialize TrainerWorkloadRequest to JSON", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message to queue: " + queue, e);
        }
    }
}

