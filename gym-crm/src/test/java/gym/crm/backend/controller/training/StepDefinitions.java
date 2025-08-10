package gym.crm.backend.controller.training;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.controller.TrainingController;
import gym.crm.backend.domain.request.TrainingCreationRequest;
import gym.crm.backend.domain.response.trainer.TrainerUpdateResponse;
import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import gym.crm.backend.exception.handler.GlobalExceptionHandler;
import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.ZoneId;
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
    private String trainerId;
    private String traineeId;
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

    @ParameterType(".*")
    public Date isoDate(String dateStr) {
        return Date.from(LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // --- Get Training Types ---
    @Given("the following training types exist:")
    public void theFollowingTrainingTypesExist(io.cucumber.datatable.DataTable dataTable) {
        trainingTypes.clear();
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            TrainingTypeResponse type = new TrainingTypeResponse(
                    row.get("name"),
                    Long.valueOf(row.get("id"))
            );
            trainingTypes.add(type);
        }
        PageImpl<TrainingTypeResponse> page = new PageImpl<>(trainingTypes);
        when(trainingService.getTrainingTypes(any())).thenReturn(page);
    }

    @When("the trainee requests all training types")
    public void theTraineeRequestAllTheTrainingTypes() throws Exception {
        result = mockMvc.perform(get("/training/trainingTypes")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @When("the trainee requests all training types but the trainee has not logged in")
    public void theTraineeRequestAllTheTrainingTypesButNotLoggedIn() throws Exception {
        isAuthenticated = false;
        doThrow(new ForbidenException("Unauthorized access")).when(trainingService).getTrainingTypes(any());
        result = mockMvc.perform(get("/training/trainingTypes")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("the response should contain the training type with ID {string} and name {string}")
    public void theResponseShouldContainTrainingTypeWithIdAndName(String id, String name) throws Exception {
        Assertions.assertEquals(200, result.getResponse().getStatus());
        PagedModel<EntityModel<TrainingTypeResponse>> responseTypes =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<PagedModel<EntityModel<TrainingTypeResponse>>>() {}
                );
        boolean found = responseTypes.getContent().stream()
                .anyMatch(type -> type.getContent().getTrainingTypeId().equals(Long.valueOf(id)) && type.getContent().getTrainingTypeName().equals(name));
        Assertions.assertTrue(found, "Training type with ID " + id + " and name " + name + " not found in response.");
    }

    @And("the response message should contain {string}")
    public void theResponseMessageShouldContain(String message) throws  Exception {
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(message), "Response does not contain expected message: " + message);
    }

    // --- Register Training ---
    @And("the trainee with ID {string} exists")
    public void theTraineeWithIdExists(String traineeId) {
        this.traineeId = traineeId;
    }
    @And("the trainer with ID {string} exists")
    public void theTrainerWithIdExists(String trainerId) {
        this.trainerId = trainerId;
    }

    @When("the trainee with ID {string} registers for training type {string}, with date {isoDate}, trainer ID {string} and duration {double} minutes")
    public void theTraineeWithIdRegistersForTrainingTypeWithDateTrainerIdAndDuration(String traineeId, String trainingTypeId, Date date, String trainerId, double duration) throws Exception {
        trainingCreationRequest.setTraineeUsername(traineeId);
        trainingCreationRequest.setTrainingDate(date);
        trainingCreationRequest.setTrainerUsername(trainerId);
        trainingCreationRequest.setTrainingDuration(duration);
        trainingCreationRequest.setTrainingName(trainingTypeId);

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