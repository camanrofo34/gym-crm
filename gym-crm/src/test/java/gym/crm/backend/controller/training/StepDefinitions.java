package gym.crm.backend.controller.training;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.controller.TrainingController;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.exception.handler.GlobalExceptionHandler;
import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    private TrainingService trainingService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private TrainingController trainingController;

    private MvcResult result;
    private List<TrainingTypeResponse> trainingTypes;
    private TrainingCreationRequest trainingCreationRequest;
    private boolean isAuthenticated;
    @Mock
    private Timer mockTimer;
    @Mock
    private Counter mockCounter;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("training.registration.counter")).thenReturn(mockCounter);
        when(meterRegistry.timer("training.registration.timer")).thenReturn(mockTimer);
        trainingController = new TrainingController(trainingService, meterRegistry);
        trainingController.setPagedResourcesAssemblerTrainingType(new PagedResourcesAssembler<>(null, null));
        objectMapper = new ObjectMapper();
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).setCustomArgumentResolvers(pageableResolver).setControllerAdvice(new GlobalExceptionHandler()).build();
        trainingTypes = new ArrayList<>();
        trainingCreationRequest = new TrainingCreationRequest();
        isAuthenticated = true;
    }

    // --- Get Training Types ---
    @Given("the following training types exist:")
    public void theFollowingTrainingTypesExist(io.cucumber.datatable.DataTable dataTable) {
        trainingTypes.clear();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            TrainingTypeResponse type = new TrainingTypeResponse(
                    row.get("name"),
                    Long.getLong(row.get("id"))
            );
            trainingTypes.add(type);
        }
        PageImpl<TrainingTypeResponse> page = new PageImpl<>(trainingTypes);
        when(trainingService.getTrainingTypes(any())).thenReturn(page);
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) throws Exception {
        result = mockMvc.perform(get(endpoint)
                .param("sort", "name,asc")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON)
        ).andReturn();
    }

    @When("I send a GET request to {string} without authorization")
    public void iSendAGetRequestToWithoutAuthorization(String endpoint) throws Exception {
        doThrow(new ForbidenException("Forbidden")).when(trainingService).getTrainingTypes(any());
        result = mockMvc.perform(get(endpoint)).andReturn();
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the response should contain the following training types:")
    public void theResponseShouldContainTheFollowingTrainingTypes(io.cucumber.datatable.DataTable dataTable) throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains("Basic Training"));
        Assertions.assertTrue(responseContent.contains("Advanced Training"));
    }

    // --- Register Training ---
    @Given("a training type with ID {string} exists with name {string}")
    public void aTrainingTypeWithIdExistsWithName(String id, String name) {
    }

    @Given("a user with ID {string} exists with first name {string}, last name {string}")
    public void aUserWithIdExists(String userId, String firstName, String lastName) {
    }

    @When("an authenticated user tries to register for the training type with ID {string} for user {string}")
    public void authenticatedUserRegistersTraining(String trainingTypeId, String userId) throws Exception {
        isAuthenticated = true;
        trainingCreationRequest.setTrainingDate(new Date());
        trainingCreationRequest.setTrainingDuration(1.0);
        trainingCreationRequest.setTrainingName(trainingTypeId);
        trainingCreationRequest.setTrainerUsername("trainerUsername");
        trainingCreationRequest.setTraineeUsername(userId);
        doNothing().when(trainingService).createTraining(any(TrainingCreationRequest.class));
        result = mockMvc.perform(post("/training/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingCreationRequest)))
                .andReturn();
    }

    @When("an authenticated user tries to register for the training type with ID {string} for non-existent user {string}")
    public void authenticatedUserRegistersTrainingNonExistentUser(String trainingTypeId, String userId) throws Exception {
        isAuthenticated = true;
        trainingCreationRequest.setTrainingDate(new Date());
        trainingCreationRequest.setTrainingDuration(1.0);
        trainingCreationRequest.setTrainingName(trainingTypeId);
        trainingCreationRequest.setTrainerUsername("trainerUsername");
        trainingCreationRequest.setTraineeUsername(userId);
        doThrow(new UserNotFoundException("User not found")).when(trainingService).createTraining(any(TrainingCreationRequest.class));
        result = mockMvc.perform(post("/training/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingCreationRequest)))
                .andReturn();
    }

    @When("a user without proper authorization tries to register for the training type with ID {string}")
    public void unauthorizedUserRegistersTraining(String trainingTypeId) throws Exception {
        isAuthenticated = false;
        trainingCreationRequest.setTrainingDate(new Date());
        trainingCreationRequest.setTrainingDuration(1.0);
        trainingCreationRequest.setTrainingName(trainingTypeId);
        trainingCreationRequest.setTrainerUsername("trainerUsername");
        trainingCreationRequest.setTraineeUsername("userId");
        doThrow(new ForbidenException("Forbidden")).when(trainingService).createTraining(any(TrainingCreationRequest.class));
        result = mockMvc.perform(post("/training/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trainingCreationRequest)))
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }
}