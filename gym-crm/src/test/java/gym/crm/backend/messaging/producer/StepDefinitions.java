package gym.crm.backend.messaging.producer;

import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.service.JwtService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@AutoConfigureMockMvc
@SpringBootTest
public class StepDefinitions {

    private String trainerUsername;
    private String traineeUsername;
    private String transactionId;
    private RestTemplate restTemplate;
    private TrainerWorkloadRequest lastReceivedWorkload;
    private TrainingCreationRequest trainingCreationRequest;
    @Autowired
    private JwtService jwtService;

    @Before
    public void setup() {
        restTemplate = new RestTemplate();
        transactionId = UUID.randomUUID().toString();
        WorkloadSpyListener.clear();
    }

    @Given("a trainer {string} and a trainee {string} exist in the system")
    public void setupUsers(String trainer, String trainee) {
        trainerUsername = trainer;
        traineeUsername = trainee;
    }

    @When("a training session is created with trainer {string} and trainee {string}")
    public void createTrainingSession(String trainer, String trainee) {
        TrainingCreationRequest request = new TrainingCreationRequest();
        request.setTrainerUsername(trainer);
        request.setTraineeUsername(trainee);
        request.setTrainingName("YOGA");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateToken(trainerUsername));
        headers.set("Transaction-Id", transactionId);

        HttpEntity<TrainingCreationRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity("http://localhost:8080/training/register", entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Then("the trainer workload should be updated in the receiving service")
    public void verifyWorkloadWasUpdated() throws InterruptedException {
        Thread.sleep(2000);
        for (int i = 0; i < 10; i++) {
            if (WorkloadSpyListener.getLastReceived() != null) break;
            Thread.sleep(500);
        }

        lastReceivedWorkload = WorkloadSpyListener.getLastReceived();

        assertNotNull(lastReceivedWorkload, "Expected a TrainerWorkloadRequest to be received.");
        assertEquals("jane.doe", lastReceivedWorkload.getTrainerUsername());
        assertEquals(60.0, lastReceivedWorkload.getTrainingDuration());
        assertThat(lastReceivedWorkload.getTrainerUsername()).isEqualTo(trainerUsername);
    }
}
