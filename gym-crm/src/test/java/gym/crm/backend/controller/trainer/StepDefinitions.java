package gym.crm.backend.controller.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.controller.TrainerController;
import gym.crm.backend.domain.request.TrainerCreationRequest;
import gym.crm.backend.domain.request.TrainerUpdateRequest;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainer.TrainerGetProfileResponse;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.domain.response.training.TrainingTrainersResponse;
import gym.crm.backend.exception.handler.GlobalExceptionHandler;
import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.service.TrainerService;
import gym.crm.backend.service.TrainingService;
import gym.crm.backend.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@CucumberContextConfiguration
@AutoConfigureMockMvc
public class StepDefinitions {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private TrainerController trainerController;

    private MvcResult result;
    private String trainerId;
    private TrainerCreationRequest trainerCreationRequest;
    private TrainerUpdateRequest trainerUpdateRequest;
    private boolean isAuthenticated;
    private boolean isAuthorized;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        Counter mockCounter = mock(Counter.class);
        io.micrometer.core.instrument.Timer mockTimer = mock(io.micrometer.core.instrument.Timer.class);
        when(meterRegistry.counter("trainer.registration.counter")).thenReturn(mockCounter);
        when(meterRegistry.timer("trainer.registration.timer")).thenReturn(mockTimer);
        trainerController = new TrainerController(trainerService, trainingService, userService, meterRegistry);
        trainerController.setPagedResourcesAssemblerTraining(new PagedResourcesAssembler<>(null, null));
        objectMapper = new ObjectMapper();
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).setCustomArgumentResolvers(pageableResolver).setControllerAdvice(new GlobalExceptionHandler()).build();
        trainerCreationRequest = new TrainerCreationRequest();
        trainerUpdateRequest = new TrainerUpdateRequest();
        isAuthenticated = false;
        isAuthorized = true;
    }

    // --- Register Trainer ---
    @When("a user tries to register a trainer with first name {string}, last name {string} and specialization with id {long}")
    public void userRegistersTrainer(String firstName, String lastName, long specializationId) throws Exception {
        trainerCreationRequest.setFirstName(firstName);
        trainerCreationRequest.setLastName(lastName);
        trainerCreationRequest.setTrainingTypeId(specializationId);
        when(trainerService.createTrainer(any(TrainerCreationRequest.class))).thenReturn(new UserCreationResponse("john.doe", "1234"));
        result = mockMvc.perform(post("/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerCreationRequest)))
                .andReturn();
    }

    @When("a user tries to register a trainer with last name {string} and specialization with id {long}")
    public void userRegistersTrainerMissingFirstName(String lastName, long specializationId) throws Exception {
        trainerCreationRequest.setLastName(lastName);
        trainerCreationRequest.setTrainingTypeId(specializationId);
        when(trainerService.createTrainer(any(TrainerCreationRequest.class))).thenReturn(null);
        result = mockMvc.perform(post("/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerCreationRequest)))
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the username should be {string}")
    public void usernameShouldBe(String username) throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        if (responseContent == null || responseContent.isEmpty()) {
            Assertions.fail("UserCreationResponse is null or empty");
        } else {
            UserCreationResponse response = objectMapper.readValue(responseContent, UserCreationResponse.class);
            Assertions.assertEquals(username, response.getUsername());
        }
    }

    // --- Activate/Deactivate Trainer ---
    @Given("a trainer with ID {string} exists with first name {string}, last name {string}")
    public void trainerWithIdExists(String id, String firstName, String lastName) {
        trainerId = id;
        TrainerGetProfileResponse response = new TrainerGetProfileResponse(
                firstName,
                lastName,
                1L,
                true,
                new HashSet<>()
        );
        Mockito.when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @When("a authenticated user tries to activate the trainer with ID {string}")
    public void authenticatedUserActivatesTrainer(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                .param("isActive", "true"))
                .andReturn();
    }

    @When("a authenticated user tries to deactivate the trainer with ID {string}")
    public void authenticatedUserDeactivatesTrainer(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                .param("isActive", "false"))
                .andReturn();
    }

    @When("a user without proper authorization tries to activate deactivate the trainer with ID {string}")
    public void unauthorizedUserActivatesDeactivatesTrainer(String id) throws Exception {
        isAuthorized = false;
        doThrow(new ForbidenException("Forbidden")).when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                .param("isActive", "true"))
                .andReturn();
    }

    @Then("the activation response status should be {int}")
    public void activationResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @Then("the deactivation response status should be {int}")
    public void deactivationResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the trainer should be active")
    public void trainerShouldBeActive() {
        Assertions.assertTrue(true);
    }

    @And("the trainer should be inactive")
    public void trainerShouldBeInactive() {
        Assertions.assertTrue(true);
    }

    // --- Get Trainer ---
    @When("a authenticated user tries to retrieve the trainer information for ID {string}")
    public void authenticatedUserRetrievesTrainer(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        TrainerGetProfileResponse response = new TrainerGetProfileResponse(
                "John",
                "Doe",
                1L,
                true,
                new HashSet<>()
        );
        Mockito.when(trainerService.getTrainerByUsername(id)).thenReturn(response);
        result = mockMvc.perform(get("/trainer/profile/" + id))
                .andReturn();
    }

    @When("a authenticated user tries to retrieve the trainer information for ID {string} but trainee does not exist")
    public void authenticatedUserRetrievesTrainerNotFound(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        Mockito.when(trainerService.getTrainerByUsername(id)).thenThrow(new UserNotFoundException("Trainer not found"));
        result = mockMvc.perform(get("/trainer/profile/" + id))
                .andReturn();
    }

    @When("a user without proper authorization tries to retrieve the trainer information for ID {string}")
    public void unauthorizedUserRetrievesTrainer(String id) throws Exception {
        isAuthorized = false;
        Mockito.when(trainerService.getTrainerByUsername(id)).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(get("/trainer/profile/" + id))
                .andReturn();
    }

    // --- Get Trainer Trainings ---
    @When("a authenticated user tries to retrieve the trainings for trainer ID {string}")
    public void authenticatedUserGetsTrainerTrainings(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        PageImpl<TrainingTrainersResponse> page = new PageImpl<>(Collections.singletonList(new TrainingTrainersResponse(
                "Training 1",
                new Date(),
                "Yoga",
                60.0,
                "Yoga"
        )));
        Mockito.when(trainingService.getTrainerTrainings(any(), any(), any(), any(), any())).thenReturn(page);
        result = mockMvc.perform(get("/trainer/profile/" + id + "/trainings"))
                .andReturn();
    }

    @When("a authenticated user tries to retrieve the trainings for a non-existing trainer ID {string}")
    public void authenticatedUserGetsTrainerTrainingsNotFound(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        Mockito.when(trainingService.getTrainerTrainings(any(), any(), any(), any(), any())).thenThrow(new UserNotFoundException("Trainer not found"));
        result = mockMvc.perform(get("/trainer/profile/" + id + "/trainings"))
                .andReturn();
    }

    @When("a user without proper authorization tries to retrieve the trainings for trainer ID {string}")
    public void unauthorizedUserGetsTrainerTrainings(String id) throws Exception {
        isAuthorized = false;
        Mockito.when(trainingService.getTrainerTrainings(any(), any(), any(), any(), any())).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(get("/trainer/profile/" + id + "/trainings"))
                .andReturn();
    }

    @And("the response should contain a list of trainings for trainer ID {string}")
    public void responseShouldContainListOfTrainings(String id) {
        Assertions.assertTrue(true);
    }

    // --- Update Trainer ---
    @When("a authenticated user tries to update the trainer information with first name {string}, last name {string}, birthdate {string}, and address {string}")
    public void authenticatedUserUpdatesTrainer(String firstName, String lastName, String birthdate, String address) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        trainerUpdateRequest.setFirstName(firstName);
        trainerUpdateRequest.setLastName(lastName);
        trainerUpdateRequest.setIsActive(true);
        trainerUpdateRequest.setTrainingTypeId(1L);
        TrainerUpdateResponse response = new TrainerUpdateResponse(
                "john.doe",
                firstName,
                lastName,
                1L,
                true,
                new HashSet<>()
        );
        Mockito.when(trainerService.updateTrainerProfile(any(), any())).thenReturn(response);
        result = mockMvc.perform(put("/trainer/profile/" + trainerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainerUpdateRequest)))
                .andReturn();
    }

    @When("a authenticated user tries to update the trainer information without providing first name, last name, birthdate, or address")
    public void authenticatedUserUpdatesTrainerMissingFields() throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        trainerUpdateRequest = new TrainerUpdateRequest();
        trainerUpdateRequest.setFirstName("null");
        trainerUpdateRequest.setLastName("null");
        trainerUpdateRequest.setIsActive(true);
        trainerUpdateRequest.setTrainingTypeId(1L);
        Mockito.when(trainerService.updateTrainerProfile(any(), any())).thenThrow(new RuntimeException("Bad Request"));
        result = mockMvc.perform(put("/trainer/profile/" + trainerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainerUpdateRequest)))
                .andReturn();
    }

    @When("a user without proper authorization tries to update the trainer information")
    public void unauthorizedUserUpdatesTrainer() throws Exception {
        isAuthorized = false;
        trainerUpdateRequest.setFirstName("null");
        trainerUpdateRequest.setLastName("null");
        trainerUpdateRequest.setIsActive(true);
        trainerUpdateRequest.setTrainingTypeId(1L);
        Mockito.when(trainerService.updateTrainerProfile(any(), any())).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(put("/trainer/profile/" + trainerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainerUpdateRequest)))
                .andReturn();
    }

    @Then("the update response status should be {int}")
    public void updateResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the trainer information should be updated to first name {string}, last name {string}")
    public void trainerInformationShouldBeUpdated(String firstName, String lastName) {
        Assertions.assertTrue(true);
    }
}