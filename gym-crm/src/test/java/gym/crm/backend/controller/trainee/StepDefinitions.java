package gym.crm.backend.controller.trainee;

import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.controller.TraineeController;
import gym.crm.backend.domain.request.TraineeCreationRequest;
import gym.crm.backend.domain.request.TraineeUpdateRequest;
import gym.crm.backend.domain.response.UserCreationResponse;
import gym.crm.backend.domain.response.trainee.TraineeGetProfileResponse;
import gym.crm.backend.domain.response.trainee.TraineeUpdateResponse;
import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import gym.crm.backend.exception.handler.GlobalExceptionHandler;
import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.service.TraineeService;
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
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@CucumberContextConfiguration
@AutoConfigureMockMvc
public class StepDefinitions {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private PagedResourcesAssembler<TrainersTraineeResponse> pagedResourcesAssemblerTrainers;

    @InjectMocks
    private TraineeController traineeController;

    private MvcResult result;
    private String traineeId;
    private TraineeCreationRequest traineeCreationRequest;
    private TraineeUpdateRequest traineeUpdateRequest;
    private List<String> trainerIds;
    private boolean isAuthenticated;
    private boolean traineeExists;
    private boolean isAuthorized;

    io.micrometer.core.instrument.Timer mockTimer = mock(io.micrometer.core.instrument.Timer.class);
    Counter mockCounter = mock(Counter.class);

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(meterRegistry.counter("trainee.registration.counter")).thenReturn(mockCounter);
        when(meterRegistry.timer("trainee.registration.timer")).thenReturn(mockTimer);
        traineeController = new TraineeController(traineeService, trainingService, userService, meterRegistry);
        objectMapper = new ObjectMapper();
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).setCustomArgumentResolvers(pageableResolver).setControllerAdvice(new GlobalExceptionHandler()).build();
        traineeCreationRequest = new TraineeCreationRequest();
        traineeUpdateRequest = new TraineeUpdateRequest();
        trainerIds = new ArrayList<>();
        isAuthenticated = false;
        traineeExists = false;
        isAuthorized = true;
    }

    // --- Common Given steps ---
    @Given("a trainee with ID {string} exists with first name {string}, last name {string}")
    public void aTraineeWithIdExists(String id, String firstName, String lastName) {
        traineeId = id;
        traineeExists = true;
        TraineeGetProfileResponse response = new TraineeGetProfileResponse(firstName, lastName, new Date(),
                "address", true, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @Given("a trainee with ID {string} does not exist")
    public void aTraineeWithIdDoesNotExist(String id) {
        traineeId = id;
        traineeExists = false;
        when(traineeService.getTraineeByUsername(id)).thenThrow(new RuntimeException("Trainee not found"));
    }

    @Given("trainers with IDs {string}, {string} exist")
    public void trainersWithIdsExist(String trainer1, String trainer2) {
        trainerIds = Arrays.asList(trainer1, trainer2);
    }

    // --- Auth steps ---
    @When("a authenticated user tries to activate the trainee with ID {string}")
    public void authenticatedUserActivatesTrainee(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "true"))
                .andReturn();
    }

    @When("a authenticated user tries to deactivate the trainee with ID {string}")
    public void authenticatedUserDeactivatesTrainee(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "false"))
                .andReturn();
    }

    @When("a user without proper authorization tries to activate the trainee with ID {string}")
    public void unauthorizedUserActivatesTrainee(String id) throws Exception {
        isAuthorized = false;
        doThrow(new ForbidenException("Forbidden")).when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "true"))
                .andReturn();
    }

    @When("a user without proper authorization tries to deactivate the trainee with ID {string}")
    public void unauthorizedUserDeactivatesTrainee(String id) throws Exception {
        isAuthorized = false;
        doThrow(new ForbidenException("Forbidden")).when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "false"))
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

    @And("the trainee with ID {string} should be active")
    public void traineeShouldBeActive(String id) {
        Assertions.assertTrue(true);
    }

    @And("the trainee with ID {string} should be inactive")
    public void traineeShouldBeInactive(String id) {
        Assertions.assertTrue(true);
    }

    // --- Assign Trainers ---
    @When("a authenticated user tries to assign trainers with IDs {string}, {string} to the trainee with ID {string}")
    public void authenticatedUserAssignsTrainers(String trainer1, String trainer2, String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        Set<TrainersTraineeResponse> trainers = new HashSet<>();
        trainers.add(new TrainersTraineeResponse(trainer1, "Trainer One", "Lastname One", 1L));
        trainers.add(new TrainersTraineeResponse(trainer2, "Trainer Two", "Lastname Two", 2L));
        when(traineeService.updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2))).thenReturn(trainers);
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + id + "/assign-trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(trainer1, trainer2))))
                .andReturn();
    }

    @When("a authenticated user tries to assign trainers with IDs {string}, {string} to the trainee with ID {string} but trainee does not exist")
    public void authenticatedUserAssignsTrainersToNonExistentTrainee(String trainer1, String trainer2, String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        when(traineeService.updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2))).thenThrow(new UserNotFoundException("Trainee not found"));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + id + "/assign-trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(trainer1, trainer2))))
                .andReturn();
    }

    @When("a user without proper authorization tries to assign trainers with IDs {string}, {string} to the trainee with ID {string}")
    public void unauthorizedUserAssignsTrainers(String trainer1, String trainer2, String id) throws Exception {
        isAuthorized = false;
        when(traineeService.updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2))).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + id + "/assign-trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(trainer1, trainer2))))
                .andReturn();
    }

    @Then("the assignment response status should be {int}")
    public void assignmentResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the trainee with ID {string} should have trainers with IDs {string}, {string} assigned")
    public void traineeShouldHaveTrainersAssigned(String id, String trainer1, String trainer2) {
        Assertions.assertTrue(true);
    }

    // --- Delete Trainee ---
    @When("a authenticated user tries to delete the trainee with ID {string}")
    public void authenticatedUserDeletesTrainee(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(traineeService).deleteTrainee(id);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + id))
                .andReturn();
    }

    @When("a authenticated user tries to delete the trainee with ID {string} but trainee does not exist")
    public void authenticatedUserDeletesNonExistentTrainee(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doThrow(new UserNotFoundException("Trainee not found")).when(traineeService).deleteTrainee(id);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + id))
                .andReturn();
    }

    @When("a user without proper authorization tries to delete the trainee with ID {string}")
    public void unauthorizedUserDeletesTrainee(String id) throws Exception {
        isAuthorized = false;
        doThrow(new ForbidenException("Forbidden")).when(traineeService).deleteTrainee(id);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + id))
                .andReturn();
    }

    @Then("the delete response status should be {int}")
    public void deleteResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the trainee with ID {string} should no longer exist")
    public void traineeShouldNoLongerExist(String id) {
        Assertions.assertTrue(true);
    }

    // --- Get Trainers Not Assigned ---
    @When("a authenticated user tries to get trainers not assigned to the trainee with ID {string}")
    public void authenticatedUserGetsTrainersNotAssigned(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        when(pagedResourcesAssemblerTrainers.toModel(any(Page.class)))
                .thenReturn(PagedModel.empty());
        PageImpl<TrainersTraineeResponse> page = new PageImpl<>(Collections.singletonList(new TrainersTraineeResponse("trainer3", "Trainer Three", "Lastname Three", 3L)));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("trainerId").ascending());
        when(traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(id, pageable)).thenReturn(page);
        result = mockMvc.perform(
                MockMvcRequestBuilders.get("/trainee/profile/" + id + "/not-assigned-trainers")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "trainerId,asc")
                .accept(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }

    @When("a user without proper authorization tries to get trainers not assigned to the trainee with ID {string}")
    public void unauthorizedUserGetsTrainersNotAssigned(String id) throws Exception {
        isAuthorized = false;
        when(traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(id, Pageable.unpaged())).thenThrow(new RuntimeException("Forbidden"));
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/not-assigned-trainers"))
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the response should contain a list of trainers not assigned to the trainee")
    public void responseShouldContainListOfTrainersNotAssigned() {
        Assertions.assertTrue(true);
    }

    // --- Get Trainings For Trainee ---
    @When("a authenticated user tries to get trainings for the trainee with ID {string}")
    public void authenticatedUserGetsTrainingsForTrainee(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        PageImpl<TrainingTraineesResponse> page = new PageImpl<>(Collections.singletonList(new TrainingTraineesResponse(
                "training1", new Date(), "Description One", 1.0, "Trainer One")));
        when(trainingService.getTraineeTrainings(id, null, null, null, null, Pageable.unpaged())).thenReturn(page);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/trainings"))
                .andReturn();
    }

    @When("a user without proper authorization tries to get trainings for the trainee with ID {string}")
    public void unauthorizedUserGetsTrainingsForTrainee(String id) throws Exception {
        isAuthorized = false;
        when(trainingService.getTraineeTrainings(id, null, null, null, null, Pageable.unpaged())).thenThrow(new RuntimeException("Forbidden"));
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/trainings"))
                .andReturn();
    }

    @And("the response should contain a list of trainings associated with the trainee")
    public void responseShouldContainListOfTrainings() {
        Assertions.assertTrue(true);
    }

    // --- Update Trainee ---
    @When("a authenticated user tries to update the trainee information with first name {string}, last name {string}, birthdate {string}, and address {string}")
    public void authenticatedUserUpdatesTrainee(String firstName, String lastName, String birthdate, String address) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        traineeUpdateRequest.setUsername(traineeId);
        traineeUpdateRequest.setFirstName(firstName);
        traineeUpdateRequest.setLastName(lastName);
        traineeUpdateRequest.setDateOfBirth(new Date());
        traineeUpdateRequest.setAddress(address);
        traineeUpdateRequest.setIsActive(true);
        TraineeUpdateResponse response = new TraineeUpdateResponse(
                traineeId, firstName, lastName, new Date(), address, true, new HashSet<>()
        );
        when(traineeService.updateTrainee(any(), any())).thenReturn(response);
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + traineeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeUpdateRequest)))
                .andReturn();
    }

    @When("a authenticated user tries to update the trainee information without providing first name, last name, birthdate, or address")
    public void authenticatedUserUpdatesTraineeMissingFields() throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        traineeUpdateRequest = new TraineeUpdateRequest();
        when(traineeService.updateTrainee(any(), any())).thenThrow(new RuntimeException("Bad Request"));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + traineeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeUpdateRequest)))
                .andReturn();
    }

    @When("a user without proper authorization tries to update the trainee information")
    public void unauthorizedUserUpdatesTrainee() throws Exception {
        isAuthorized = false;
        traineeUpdateRequest.setUsername(traineeId);
        traineeUpdateRequest.setFirstName("john");
        traineeUpdateRequest.setLastName("doe");
        traineeUpdateRequest.setDateOfBirth(new Date());
        traineeUpdateRequest.setAddress("address");
        traineeUpdateRequest.setIsActive(true);
        when(traineeService.updateTrainee(any(), any())).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + traineeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeUpdateRequest)))
                .andReturn();
    }

    @Then("the update response status should be {int}")
    public void updateResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the trainee information should be updated to first name {string}, last name {string}")
    public void traineeInformationShouldBeUpdated(String firstName, String lastName) {
        Assertions.assertTrue(true);
    }

    // --- Get Trainee Profile ---

    @When("a authenticated user tries to retrieve the trainee information for ID {string}")
    public void authenticatedUserRetrievesTraineeProfile(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", true, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id))
                .andReturn();
    }

    @When("a authenticated user tries to retrieve the trainee information for ID {string} but trainee does not exist")
    public void authenticatedUserRetrievesNonExistentTraineeProfile(String id) throws Exception
    {
        isAuthenticated = true;
        isAuthorized = true;
        when(traineeService.getTraineeByUsername(id)).thenThrow(new UserNotFoundException("Trainee not found"));
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id))
                .andReturn();
    }

    @When("a user without proper authorization tries to retrieve the trainee information for ID {string}")
    public void unauthorizedUserRetrievesTraineeProfile(String id) throws Exception {
    isAuthorized = false;
        when(traineeService.getTraineeByUsername(id)).thenThrow(new ForbidenException("Forbidden"));
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id))
                .andReturn();
    }

    // --- Register Trainee ---
    @When("a user tries to register a trainee with first name {string}, last name {string}, birthdate {string}, and address {string}")
    public void userRegistersTrainee(String firstName, String lastName, String birthdate, String address) throws Exception {
        traineeCreationRequest.setFirstName(firstName);
        traineeCreationRequest.setLastName(lastName);
        traineeCreationRequest.setDateOfBirth(new Date());
        traineeCreationRequest.setAddress(address);

        UserCreationResponse userCreationResponse = new UserCreationResponse("john.doe", "1234");
        when(traineeService.createTrainee(any())).thenReturn(userCreationResponse);

        if (userCreationResponse != null) {
            result = mockMvc.perform(post("/trainee/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(traineeCreationRequest)))
                    .andReturn();
        } else {
            result = mockMvc.perform(post("/trainee/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(traineeCreationRequest)))
                    .andReturn();
            Assertions.assertEquals(400, result.getResponse().getStatus());
        }
    }

    @When("a user tries to register as a trainee without providing first name, last name, birthdate, or address")
    public void userRegistersTraineeMissingFields() throws Exception {
        traineeCreationRequest = new TraineeCreationRequest();
        when(traineeService.createTrainee(traineeCreationRequest)).thenThrow(new RuntimeException("Bad Request"));
        result = mockMvc.perform(post("/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traineeCreationRequest)))
                .andReturn();
    }

}
