package gym.crm.backend.controller.trainer;

import com.fasterxml.jackson.core.type.TypeReference;
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
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.TrainingTypeNotFoundException;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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

    // --- Common Given steps ---
    @Given("a trainer with ID {string} exists with first name {string}, last name {string} and specialization {string}")
    public void aTrainerWithIdExists(String id, String firstName, String lastName, String specialization) {
        trainerId = id;
        TrainerGetProfileResponse response = new TrainerGetProfileResponse(firstName, lastName, 1L, true, new HashSet<>());
        when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the response message should contain {string}")
    public void responseMessageShouldContain(String message) throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        System.out.println(responseContent);
        Assertions.assertTrue(responseContent.contains(message), "Response should contain message: " + message);
    }

    // --- Activate Deactivate Trainer ---
    @Given("a trainer with ID {string} exists in the system")
    public void aTraineeWithIdExistsInSystem(String id) {
        trainerId = id;
    }

    @When("a trainer with ID {string} tries to activate their account")
    public void traineeActivatesAccount(String id) throws Exception {
        trainerId = id;
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                        .param("isActive", "true"))
                .andReturn();
        TrainerGetProfileResponse response = new TrainerGetProfileResponse("John", "Doe", 1L, true, new HashSet<>());
        when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @When("a trainer with ID {string} tries to deactivate their account")
    public void traineeDeactivatesAccount(String id) throws Exception {
        trainerId = id;
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                        .param("isActive", "false"))
                .andReturn();
        TrainerGetProfileResponse response = new TrainerGetProfileResponse("John", "Doe", 1L, false, new HashSet<>());
        when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @When("a trainer with ID {string} tries to activate their account but the trainer has not login before")
    public void traineeActivatesAccountButTheTraineeHasNotLoginBefore(String id) throws Exception {
        trainerId = id;
        isAuthenticated = false;
        isAuthorized = true;
        doThrow(new ForbidenException("Unauthorized access")).when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                        .param("isActive", "true"))
                .andReturn();
        TrainerGetProfileResponse response = new TrainerGetProfileResponse("John", "Doe", 1L, false, new HashSet<>());
        when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @When("a trainer with ID {string} tries to deactivate their account but the trainer has not login before")
    public void traineeDeactivatesAccountButTheTraineeHasNotLoginBefore(String id) throws Exception {
        trainerId = id;
        isAuthenticated = false;
        isAuthorized = true;
        doThrow(new ForbidenException("Unauthorized access")).when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainer/profile/" + id + "/activate-deactivate")
                        .param("isActive", "false"))
                .andReturn();
        TrainerGetProfileResponse response = new TrainerGetProfileResponse("John", "Doe", 1L, true, new HashSet<>());
        when(trainerService.getTrainerByUsername(id)).thenReturn(response);
    }

    @And("the trainer with ID {string} should be active")
    public void traineeShouldBeActive(String id) throws Exception {
        result = mockMvc.perform(get("/trainer/profile/" + id))
                .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        EntityModel<TrainerGetProfileResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TrainerGetProfileResponse>>() {}
        );

        TrainerGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertTrue(response.isActive(), "Trainee should be active");
    }

    @And("the trainer with ID {string} should be inactive")
    public void traineeShouldBeInactive(String id) throws  Exception {
        result = mockMvc.perform(get("/trainer/profile/" + id))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        EntityModel<TrainerGetProfileResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TrainerGetProfileResponse>>() {}
        );

        TrainerGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertFalse(response.isActive(), "Trainee should be active");
    }

    // --- Get Trainer Profile ---
    @When("the trainer with ID {string} retrieve the trainer information for ID {string}")
    public void retrieveTrainerInformation(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        if (!trainerId.equals(targetId)) {
            doThrow(new ProfileNotFoundException("Trainer not found")).when(trainerService).getTrainerByUsername(any());
        }
        result = mockMvc.perform(get("/trainer/profile/" + targetId))
                .andReturn();
    }

    @When("the trainer with ID {string} retrieve the trainer information for ID {string} but the trainer has not logged in before")
    public void retrieveTrainerInformationButNotLoggedIn(String id, String targetId) throws Exception {
        isAuthenticated = false;
        isAuthorized = true;
        doThrow(new ForbidenException("Unauthorized access")).when(trainerService).getTrainerByUsername(targetId);
        result = mockMvc.perform(get("/trainer/profile/" + trainerId))
                .andReturn();
    }

    @And("the response should contain the trainer's first name {string}")
    public void responseShouldContainFirstName(String firstName) throws Exception {
        Assertions.assertEquals(200, result.getResponse().getStatus());
        EntityModel<TrainerGetProfileResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TrainerGetProfileResponse>>() {}
        );
        TrainerGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertEquals(firstName, response.getFirstName(), "First name should match");
    }

    // --- Get Trainer Trainings ---
    @And("trainings with IDs {string}, {string} exist and are assigned to trainer with ID {string}")
    public void trainingsExistAndAssignedToTrainer(String trainingId1, String trainingId2, String trainerId) {
        List<TrainingTrainersResponse> trainings = new ArrayList<>();
        trainings.add(new TrainingTrainersResponse(trainingId1, new Date(), "Description 1", 20.0, "Jane"));
        trainings.add(new TrainingTrainersResponse(trainingId2, new Date(), "Description 2", 20.0, "Jane"));
        when(trainingService.getTrainerTrainings(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(trainings));
    }

    @When("the trainer with ID {string} retrieves their trainings")
    public void retrieveTrainerTrainings(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        if (!trainerId.equals(id)) {
            doThrow(new ProfileNotFoundException("Trainer not found")).when(trainingService).getTrainerTrainings(any(), any(), any(), any(), any());
        }
        result = mockMvc.perform(get("/trainer/profile/" + id + "/trainings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "date,desc"))
                .andReturn();
    }

    @When("the trainer with ID {string} retrieves their trainings but the trainer has not logged in before")
    public void retrieveTrainerTrainingsButNotLoggedIn(String id) throws Exception {
        isAuthenticated = false;
        isAuthorized = true;
        doThrow(new ForbidenException("Unauthorized access")).when(trainingService).getTrainerTrainings(any(), any(), any(), any(), any());
        result = mockMvc.perform(get("/trainer/profile/" + id + "/trainings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "date,desc"))
                .andReturn();
    }

    @And("the response should contain training IDs {string}, {string}")
    public void responseShouldContainTrainingIds(String trainingId1, String trainingId2) throws Exception {
        Assertions.assertEquals(200, result.getResponse().getStatus());
        PagedModel<EntityModel<TrainingTrainersResponse>> trainings = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<PagedModel<EntityModel<TrainingTrainersResponse>>>() {}
        );
        Assertions.assertNotNull(trainings, "Trainings list should not be null");
        Assertions.assertEquals(2, trainings.getContent().size(), "There should be two trainings in the response");
        Assertions.assertTrue(trainings.getContent().stream().anyMatch(t -> t.getContent().getTrainingName().equals(trainingId1)), "Training ID " + trainingId1 + " should be present");
        Assertions.assertTrue(trainings.getContent().stream().anyMatch(t -> t.getContent().getTrainingName().equals(trainingId2)), "Training ID " + trainingId2 + " should be present");
    }

    // --- Register Trainer ---
    @When("the new trainer registers with first name {string}, last name {string} and specialization {string}")
    public void registerNewTrainer(String firstName, String lastName, String specialization) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        trainerCreationRequest.setFirstName(firstName);
        trainerCreationRequest.setLastName(lastName);
        if (specialization.equals("UnknownSpecialization")) {
            trainerCreationRequest.setTrainingTypeId(1L);
            doThrow(new TrainingTypeNotFoundException("Specialization not found")).when(trainerService).createTrainer(any());
        }else {
            trainerCreationRequest.setTrainingTypeId(1L);
            UserCreationResponse response = new UserCreationResponse("alice.johnson", "username");
            when(trainerService.createTrainer(any())).thenReturn(response);
        }
        result = mockMvc.perform(post("/trainer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerCreationRequest)))
                .andReturn();
    }

    @And("the response should contain as username {string}")
    public void responseShouldContainUsername(String username) throws Exception {
        EntityModel<UserCreationResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<UserCreationResponse>>() {}
        );
        Assertions.assertNotNull(entityModel.getContent(), "Response content should not be null");
        Assertions.assertEquals(username, entityModel.getContent().getUsername(), "Username should match");
    }

    // --- Update Trainer Profile ---
    @When("the trainer with ID {string} updates their profile with first name {string}, last name {string}, and specialization {string}")
    public void updateTrainerProfile(String id, String firstName, String lastName, String specialization) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        trainerUpdateRequest = new TrainerUpdateRequest();
        trainerUpdateRequest.setFirstName(firstName);
        trainerUpdateRequest.setLastName(lastName);
        trainerUpdateRequest.setIsActive(true);
        if (specialization.equals("NonExistentSpecialization")) {
            trainerUpdateRequest.setTrainingTypeId(1L);
            doThrow(new TrainingTypeNotFoundException("Specialization not found")).when(trainerService).updateTrainerProfile(any(), any());
        } else if (!trainerId.equals(id)) {
            trainerUpdateRequest.setTrainingTypeId(1L);
            doThrow(new ProfileNotFoundException("Trainer not found")).when(trainerService).updateTrainerProfile(any(), any());
        }
        else {
            trainerUpdateRequest.setTrainingTypeId(1L);
            when(trainerService.updateTrainerProfile(any(), any())).thenReturn(new TrainerUpdateResponse(
                    id, firstName, lastName, 1L, true, new HashSet<>()
            ));
        }
        result = mockMvc.perform(put("/trainer/profile/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerUpdateRequest)))
                .andReturn();
    }

    @When("the trainer with ID {string} updates their profile with first name {string}, last name {string}, and specialization {string} but the trainer has not logged in before")
    public void updateTrainerProfileButNotLoggedIn(String id, String firstName, String lastName, String specialization) throws Exception {
        isAuthenticated = false;
        isAuthorized = true;
        trainerUpdateRequest = new TrainerUpdateRequest();
        trainerUpdateRequest.setFirstName(firstName);
        trainerUpdateRequest.setLastName(lastName);
        trainerUpdateRequest.setTrainingTypeId(1L);
        trainerUpdateRequest.setIsActive(true);
        doThrow(new ForbidenException("Unauthorized access")).when(trainerService).updateTrainerProfile(any(), any());
        result = mockMvc.perform(put("/trainer/profile/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerUpdateRequest)))
                .andReturn();
    }

    @And("the update response should contain the trainer's ID {string}")
    public void responseShouldContainTrainerId(String id) throws Exception {
        Assertions.assertEquals(200, result.getResponse().getStatus());
        EntityModel<TrainerUpdateResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TrainerUpdateResponse>>() {}
        );
        TrainerUpdateResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertEquals(id, response.getUsername(), "Trainer ID should match");
    }

    @And("the update response should contain the trainer's first name {string}")
    public void responseShouldContainFirstNameInUpdate(String firstName) throws Exception {
        Assertions.assertEquals(200, result.getResponse().getStatus());
        EntityModel<TrainerUpdateResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TrainerUpdateResponse>>() {}
        );
        TrainerUpdateResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertEquals(firstName, response.getFirstName(), "First name should match");
    }


}