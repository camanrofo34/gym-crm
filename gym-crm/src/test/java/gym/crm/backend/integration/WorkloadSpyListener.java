package gym.crm.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import jakarta.jms.Message;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class WorkloadSpyListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Getter
    private static TrainerWorkloadRequest lastReceived;

    @JmsListener(destination = "${queue.trainer-workload}", concurrency = "1-1")
    public void intercept(@Payload String request, Message message) throws Exception {
        lastReceived = objectMapper.readValue(request, TrainerWorkloadRequest.class);
    }

    public static void clear() {
        lastReceived = null;
    }
}


