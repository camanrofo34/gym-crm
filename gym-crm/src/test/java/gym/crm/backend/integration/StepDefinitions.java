package gym.crm.backend.integration;

import gym.crm.backend.domain.entities.TrainingType;
import gym.crm.backend.domain.entities.TrainingTypes;
import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerWorkloadRequest;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.repository.TrainingTypeRepository;
import gym.crm.backend.service.JwtService;
import gym.crm.backend.service.TraineeService;
import gym.crm.backend.service.TrainerService;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CucumberContextConfiguration
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StepDefinitions {
    @LocalServerPort
    private int port;
    private String baseUrl;
    private String trainerUsername;
    private String traineeUsername;
    private String transactionId;
    private RestTemplate restTemplate;
    private TrainerWorkloadRequest lastReceivedWorkload;
    private TrainingCreationRequest trainingCreationRequest;
    private UserCreationResponse userCreationResponse;
    private TraineeCreationRequest traineeCreationRequest = new TraineeCreationRequest();
    private TrainerCreationRequest trainerCreationRequest = new TrainerCreationRequest();
    private ResponseEntity<?> responseEntity;

    @Autowired
    private EntityManager em;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private TrainerService trainerService;
    @Autowired
    private TraineeService traineeService;
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + port;
        restTemplate = new RestTemplate();
        transactionId = UUID.randomUUID().toString();
        WorkloadSpyListener.clear();
    }

    @ParameterType(".*")
    public Date isoDate(String dateStr) {
        return Date.from(LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Given("a trainer {string} and a trainee {string} exist in the system")
    public void setupUsers(String trainer, String trainee) {
        trainerUsername = trainer;
        traineeUsername = trainee;
        traineeCreationRequest.setFirstName("John");
        traineeCreationRequest.setLastName("Doe");
        traineeCreationRequest.setAddress("123 Main St");
        traineeCreationRequest.setDateOfBirth(new Date());
        trainerCreationRequest.setFirstName("Jane");
        trainerCreationRequest.setLastName("Doe");
        trainerCreationRequest.setTrainingTypeId(1L);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TrainerCreationRequest> entity = new HttpEntity<>(trainerCreationRequest, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl + "/trainer/register", entity, Void.class);
        HttpEntity<TraineeCreationRequest> entity1 = new HttpEntity<>(traineeCreationRequest, headers);
        ResponseEntity<Void> response1 = restTemplate.postForEntity(baseUrl + "/trainee/register", entity1, Void.class);
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
        headers.set("Authorization", "Bearer " + jwtService.generateToken(traineeUsername));
        headers.set("Transaction-Id", transactionId);

        HttpEntity<TrainingCreationRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Void> response = restTemplate.postForEntity(baseUrl + "/training/register", entity, Void.class);

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
        assertEquals(trainerUsername, lastReceivedWorkload.getTrainerUsername());
        assertEquals(60.0, lastReceivedWorkload.getTrainingDuration());
        assertThat(lastReceivedWorkload.getTrainerUsername()).isEqualTo(trainerUsername);
    }


    @When("the new trainer registers with first name {string}, last name {string} and specialization {string}")
    public void registerNewTrainer(String firstName, String lastName, String specialization) {
        trainerCreationRequest.setFirstName(firstName);
        trainerCreationRequest.setLastName(lastName);
        if (!Objects.equals(specialization, "INVALID_TYPE")) {
            trainerCreationRequest.setTrainingTypeId(1L);
        }else{
            trainerCreationRequest.setTrainingTypeId(999L);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TrainerCreationRequest> entity = new HttpEntity<>(trainerCreationRequest, headers);
        try {
            ResponseEntity<EntityModel<UserCreationResponse>> response =
                    restTemplate.exchange(
                            baseUrl + "/trainer/register",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<EntityModel<UserCreationResponse>>() {
                            }
                    );
            responseEntity = response;
            userCreationResponse = response.getBody().getContent();
        }catch (Exception e){
            responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            userCreationResponse = null;
        }
    }

    @When("a training session is created without a date")
    public void createTrainingSessionWithoutDate() {
        TrainingCreationRequest request = new TrainingCreationRequest();
        request.setTrainerUsername(trainerUsername);
        request.setTraineeUsername(traineeUsername);
        request.setTrainingName("YOGA");
        request.setTrainingDuration(60.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtService.generateToken(traineeUsername));
        headers.set("Transaction-Id", transactionId);

        HttpEntity<TrainingCreationRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(baseUrl + "/training/register", entity, Void.class);
        } catch (Exception e) {
            responseEntity = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @When("a training session is created without the trainee being logged in")
    public void createTrainingSessionWithoutTraineeLoggedIn() {
        TrainingCreationRequest request = new TrainingCreationRequest();
        request.setTrainerUsername(trainerUsername);
        request.setTraineeUsername(traineeUsername);
        request.setTrainingName("YOGA");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Transaction-Id", transactionId);

        HttpEntity<TrainingCreationRequest> entity = new HttpEntity<>(request, headers);
        try {
            restTemplate.postForEntity(baseUrl + "/training/register", entity, Void.class);
        } catch (Exception e) {
            responseEntity = ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @And("the response message should contain {string}")
    public void verifyResponseMessage(String expectedMessage) {
        assertNotNull(responseEntity, "ResponseEntity should not be null");
        assertThat(responseEntity.getBody()).isInstanceOf(String.class);
        String responseBody = (String) responseEntity.getBody();
        System.out.println(responseBody);
        assertThat(responseBody).contains(expectedMessage);
    }

    @Then("the response status should be {int}")
    public void verifyResponseStatus(int expectedStatus) {
        assertNotNull(responseEntity, "ResponseEntity should not be null");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(expectedStatus));
    }

    @And("the response should contain the trainer's unique ID {string}")
    public void verifyTrainerId(String expectedId) {
        assertNotNull(userCreationResponse, "UserCreationResponse should not be null");
        assertThat(userCreationResponse.getUsername()).isEqualTo(expectedId);
    }


}
