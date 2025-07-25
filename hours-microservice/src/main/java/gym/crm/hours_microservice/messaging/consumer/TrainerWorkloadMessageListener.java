package gym.crm.hours_microservice.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.exception.types.forbidden.InvalidateTokenException;
import gym.crm.hours_microservice.service.JwtService;
import gym.crm.hours_microservice.service.TrainerWorkloadService;
import jakarta.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrainerWorkloadMessageListener {

    private final TrainerWorkloadService trainerWorkloadService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public TrainerWorkloadMessageListener(TrainerWorkloadService trainerWorkloadService,
                                          ObjectMapper objectMapper,
                                          JwtService jwtService) {
        this.trainerWorkloadService = trainerWorkloadService;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    @JmsListener(destination = "${queue.trainer-workload}", concurrency = "3-10")
    public void receiveMessage(@Payload String request, Message message) {
        try {
            String transactionId = message.getStringProperty("Transaction-Id");
            String bearerToken = message.getStringProperty("Authorization");
            MDC.put("transactionId", transactionId);
            if (jwtService.validateToken(bearerToken)) {
                TrainerWorkloadRequest workloadRequest = objectMapper.readValue(request, TrainerWorkloadRequest.class);
                log.info("Transaction ID: {} - Updating trainer workload for request: {}", transactionId, workloadRequest.getTrainerUsername());
                trainerWorkloadService.updateTrainerWorkload(workloadRequest);
                log.info("Transaction ID: {} - Successfully updated trainer workload", transactionId);
            }else {
                log.warn("Invalid token for transaction ID: {}", transactionId);
                throw new InvalidateTokenException("Invalid token");
            }
        } catch (Exception e) {
            log.error("Failed to process trainer workload: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

