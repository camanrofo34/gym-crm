package gym.crm.backend.integration.feign;

import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${trainer.workload.url}")
public interface TrainerWorkloadClient {

    @PostMapping("/api/v1/working-hours")
    void sendTrainerWorkload(
            @RequestBody TrainerWorkloadRequest request,
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("Transaction-Id") String transactionId
    );
}
