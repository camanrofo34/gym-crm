package gym.crm.hours_microservice.controller.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.hours_microservice.exception.types.forbidden.InvalidateTokenException;
import gym.crm.hours_microservice.messaging.consumer.TrainerWorkloadMessageListener;
import gym.crm.hours_microservice.service.JwtService;
import gym.crm.hours_microservice.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

@CucumberContextConfiguration
public class StepDefinitions {

    private TrainerWorkloadMessageListener listener;
    private TrainerWorkloadService workloadService;
    private JwtService jwtService;
    private Message mockMessage;
    private String token;
    private String requestBody;
    private final Map<String, Exception> scenarioContext = new HashMap<>();

    @Before
    public void setup() {
        workloadService = Mockito.mock(TrainerWorkloadService.class);
        jwtService = Mockito.mock(JwtService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        listener = new TrainerWorkloadMessageListener(workloadService, objectMapper, jwtService);
    }

    @Given("a valid JWT token")
    public void a_valid_jwt_token() {
        token = "valid.token.jwt";
        Mockito.when(jwtService.validateToken(token)).thenReturn(true);
    }

    @Given("an invalid JWT token")
    public void an_invalid_jwt_token() {
        token = "invalid.token.jwt";
        Mockito.when(jwtService.validateToken(token)).thenReturn(false);
    }

    @And("a valid trainer workload message")
    public void a_valid_trainer_workload_message() throws JMSException {
        requestBody = """
                {
                  "trainerUsername": "Camilo.Rodriguez",
                  "trainerFirstName": "Camilo",
                  "trainerLastName": "Rodriguez",
                  "isActive": true,
                  "trainingDate": "2023-10-01",
                  "trainingDuration": 1.5,
                  "actionType": "ADD"
                }
                """;
        mockMessage = Mockito.mock(Message.class);
        Mockito.when(mockMessage.getStringProperty("Transaction-Id")).thenReturn("txn-123");
        Mockito.when(mockMessage.getStringProperty("Authorization")).thenReturn(token);
    }

    @When("the message is sent to the queue")
    public void the_message_is_sent_to_the_queue() {
        try {
            listener.receiveMessage(requestBody, mockMessage);
        } catch (Exception e) {
            scenarioContext.put("error", e);
        }
    }

    @Then("the trainer workload should be updated")
    public void the_trainer_workload_should_be_updated() throws Exception {
        Mockito.verify(workloadService).updateTrainerWorkload(Mockito.any());
    }

    @Then("an invalid token error should be logged")
    public void an_invalid_token_error_should_be_logged() {
        Exception error = scenarioContext.get("error");
        Assertions.assertInstanceOf(RuntimeException.class, error);
        Assertions.assertInstanceOf(InvalidateTokenException.class, error.getCause());
    }
}
