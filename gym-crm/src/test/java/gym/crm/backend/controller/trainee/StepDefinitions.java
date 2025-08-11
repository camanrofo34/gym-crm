package gym.crm.backend.controller.trainee;

import com.fasterxml.jackson.core.type.TypeReference;
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
import gym.crm.backend.exception.types.notFound.ProfileNotFoundException;
import gym.crm.backend.exception.types.notFound.UserNotFoundException;
import gym.crm.backend.exception.types.timeout.UncheckedSocketTimeoutException;
import gym.crm.backend.service.TraineeService;
import gym.crm.backend.service.TrainingService;
import gym.crm.backend.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.ZoneId;
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

    @Mock
    private PagedResourcesAssembler<TrainingTraineesResponse> pagedResourcesAssemblerTrainings;

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
        traineeController.setPagedResourcesAssemblerTrainers(pagedResourcesAssemblerTrainers);
        traineeController.setPagedResourcesAssemblerTraining(pagedResourcesAssemblerTrainings);
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

    @ParameterType(".*")
    public Date isoDate(String dateStr) {
        return Date.from(LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // --- Common Given steps ---
    @Given("a trainee with ID {string} exists with first name {string}, last name {string}, birthdate {isoDate}, and address {string}")
    public void aTraineeWithIdExists(String id, String firstName, String lastName, Date birthdate, String address) {
        traineeId = id;
        traineeExists = true;
        TraineeGetProfileResponse response = new TraineeGetProfileResponse(firstName, lastName, birthdate,
                address, true, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @Then("the response status should be {int}")
    public void responseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    // --- Register Trainee ---
    @When("a user tries to register a trainee with first name {string}, last name {string}, birthdate {isoDate}, and address {string}")
    public void userRegistersTrainee(String firstName, String lastName, Date birthdate, String address) throws Exception {
        traineeCreationRequest = new TraineeCreationRequest();
        traineeCreationRequest.setFirstName(firstName);
        traineeCreationRequest.setLastName(lastName);
        traineeCreationRequest.setDateOfBirth(birthdate);
        traineeCreationRequest.setAddress(address);

        UserCreationResponse userCreationResponse = new UserCreationResponse("john.doe", "1234");
        when(traineeService.createTrainee(any())).thenReturn(userCreationResponse);

        result = mockMvc.perform(post("/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traineeCreationRequest)))
                .andReturn();
    }

    @And("the response message should contain {string}")
    public void responseMessageShouldContain(String message) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains(message), "Response body should contain: " + message);
    }

    // --- Activate/Deactivate Trainee ---
    @Given("a trainee with ID {string} exists in the system")
    public void aTraineeWithIdExistsInSystem(String id) {
        traineeId = id;
        traineeExists = true;
    }

    @When("a trainee with ID {string} tries to activate their account")
    public void traineeActivatesAccount(String id) throws Exception {
        traineeId = id;
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "true"))
                .andReturn();
        TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", true, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @When("a trainee with ID {string} tries to deactivate their account")
    public void traineeDeactivatesAccount(String id) throws Exception {
        traineeId = id;
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                .param("isActive", "false"))
                .andReturn();
        TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", false, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @When("a trainee with ID {string} tries to activate their account but the trainee has not login before")
    public void traineeActivatesAccountButTheTraineeHasNotLoginBefore(String id) throws Exception {
        traineeId = id;
        isAuthenticated = false;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, true);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                        .param("isActive", "true"))
                .andReturn();
        TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", false, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @When("a trainee with ID {string} tries to deactivate their account but the trainee has not login before")
    public void traineeDeactivatesAccountButTheTraineeHasNotLoginBefore(String id) throws Exception {
        traineeId = id;
        isAuthenticated = false;
        isAuthorized = true;
        doNothing().when(userService).activateDeactivateUser(id, false);
        result = mockMvc.perform(patch("/trainee/profile/" + id + "/activate-deactivate")
                        .param("isActive", "false"))
                .andReturn();
        TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", true, new HashSet<>());
        when(traineeService.getTraineeByUsername(id)).thenReturn(response);
    }

    @And("the trainee with ID {string} should be active")
    public void traineeShouldBeActive(String id) throws Exception {
        result = mockMvc.perform(get("/trainee/profile/" + id))
                .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());

        EntityModel<TraineeGetProfileResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TraineeGetProfileResponse>>() {}
        );

        TraineeGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertTrue(response.isActive(), "Trainee should be active");
    }

    @And("the trainee with ID {string} should be inactive")
    public void traineeShouldBeInactive(String id) throws  Exception {
        result = mockMvc.perform(get("/trainee/profile/" + id))
                .andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
        EntityModel<TraineeGetProfileResponse> entityModel = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<EntityModel<TraineeGetProfileResponse>>() {}
        );

        TraineeGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertFalse(response.isActive(), "Trainee should be active");
    }

    // --- Assign Trainers to Trainee ---
    @And("trainers with IDs {string}, {string} exist")
    public void trainersWithIdsExist(String trainer1, String trainer2) {
        trainerIds.add(trainer1);
        trainerIds.add(trainer2);
    }

    @When("the trainee with ID {string} assigns trainers with IDs {string}, {string} to themselves")
    public void traineeAssignsTrainersToThemselves(String id, String trainer1, String trainer2) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        Set<TrainersTraineeResponse> trainers = new HashSet<>();
        if (id.equals(traineeId)) {
            when(traineeService.updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2))).thenReturn(trainers);
        }else{
            doThrow(new UserNotFoundException("Trainee not found")).when(traineeService).updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2));
        }
        trainers.add(new TrainersTraineeResponse(trainer1, "Trainer One", "Lastname One", 1L));
        trainers.add(new TrainersTraineeResponse(trainer2, "Trainer Two", "Lastname Two", 2L));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + id + "/assign-trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(trainer1, trainer2))))
                .andReturn();
    }

    @When("the trainee with ID {string} assigns trainers with IDs {string}, {string} to themselves but the trainee has not logged in before")
    public void traineeAssignsTrainersToThemselvesButTheTraineeHasNotLoggedInBefore(String id, String trainer1, String trainer2) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        when(traineeService.updateTrainersTraineeList(id, Arrays.asList(trainer1, trainer2)))
                .thenThrow(new ForbidenException("Unauthorized access"));
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + id + "/assign-trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(trainer1, trainer2))))
                .andReturn();
    }

    @And("the assignment response message should contain trainers with IDs {string}, {string}")
    public void assignmentResponseMessageShouldContainTrainers(String trainer1, String trainer2) throws Exception {
        String json = result.getResponse().getContentAsString();

        CollectionModel<TrainersTraineeResponse> collectionModel = objectMapper.readValue(
                json,
                new TypeReference<CollectionModel<TrainersTraineeResponse>>() {}
        );

        Collection<TrainersTraineeResponse> trainers = collectionModel.getContent();

        Assertions.assertTrue(
                trainers.stream().anyMatch(t -> t.getTrainerUsername().equals(trainer1)),
                "Response should contain trainer ID: " + trainer1
        );
        Assertions.assertTrue(
                trainers.stream().anyMatch(t -> t.getTrainerUsername().equals(trainer2)),
                "Response should contain trainer ID: " + trainer2
        );
    }

    @And("the assignment response message should contain {string}")
    public void assignmentResponseMessageShouldContain(String message) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseBody.contains(message), "Response body should contain: " + message);
    }

    // --- Delete Trainee ---
    @When("the trainee with ID {string} tries to delete his trainee account with ID {string}")
    public void traineeDeletesAccount(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doNothing().when(traineeService).deleteTrainee(targetId);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + id))
                .andReturn();
    }

    @When("the trainee with ID {string} tries to delete a trainee account with ID {string}")
    public void traineeDeletesAnotherAccount(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doThrow(new UserNotFoundException("Trainee not found")).when(traineeService).deleteTrainee(targetId);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + targetId))
                .andReturn();
    }

    @When("the trainee with ID {string} tries to delete a trainee account with ID {string} but the trainee has not logged in before")
    public void traineeDeletesAccountButHasNotLoggedInBefore(String id, String targetId) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        doThrow(new ForbidenException("Unauthorized access")).when(traineeService).deleteTrainee(targetId);
        result = mockMvc.perform(MockMvcRequestBuilders.delete("/trainee/profile/" + targetId))
                .andReturn();
    }

    @And("the trainee with ID {string} should no longer exist")
    public void traineeShouldNotExist(String id) throws Exception {
        when(traineeService.getTraineeByUsername(id)).thenThrow(new UserNotFoundException("Trainee not found"));
        result = mockMvc.perform(get("/trainee/profile/" + id))
                .andReturn();
        Assertions.assertEquals(404, result.getResponse().getStatus(), "Trainee should not exist");
    }


    // --- Get Trainee ---
    @When("the trainee with ID {string} retrieve the trainee information for ID {string}")
    public void traineeRetrievesProfile(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        if (traineeId.equals(targetId)) {
            TraineeGetProfileResponse response = new TraineeGetProfileResponse("John", "Doe", new Date(), "123 Street", true, new HashSet<>());
            when(traineeService.getTraineeByUsername(targetId)).thenReturn(response);
        }else{
            doThrow(new UserNotFoundException("Trainee not found")).when(traineeService).getTraineeByUsername(targetId);
        }
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + targetId))
                .andReturn();
    }

    @When("the trainee with ID {string} retrieve the trainee information for ID {string} but the trainee has not logged in before")
    public void traineeRetrievesProfileButHasNotLoggedInBefore(String id, String targetId) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        doThrow(new ForbidenException("Unauthorized access")).when(traineeService).getTraineeByUsername(targetId);
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + targetId))
                .andReturn();
    }

    @When("the trainee with ID {string} retrieve the trainee information for ID {string} but there is a database error")
    public void traineeRetrievesProfileButDatabaseError(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doThrow(new DataAccessResourceFailureException("Data is not accessible in this moment")).when(traineeService).getTraineeByUsername(targetId);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + targetId))
                .andReturn();
    }

    @When("the trainee with ID {string} retrieve the trainee information for ID {string} but there is a timeout error while answering")
    public void traineeRetrievesProfileButTimeoutError(String id, String targetId) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        doThrow(new UncheckedSocketTimeoutException("The request took too long to process")).when(traineeService).getTraineeByUsername(targetId);

        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + targetId))
                .andReturn();
    }

    @And("the response should contain the trainee's first name {string}")
    public void responseShouldContainTraineeFirstName(String firstName) throws Exception {
        String json = result.getResponse().getContentAsString();
        EntityModel<TraineeGetProfileResponse> entityModel = objectMapper.readValue(
                json,
                new TypeReference<EntityModel<TraineeGetProfileResponse>>() {}
        );
        TraineeGetProfileResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertEquals(firstName, response.getFirstName(), "First name should match");
    }

    // --- Get Trainers Not Assigned To Trainee ---
    @And("trainers with IDs {string}, {string}, {string} exist")
    public void trainersWithIdsExist(String trainer1, String trainer2, String trainer3) {
        trainerIds.add(trainer1);
        trainerIds.add(trainer2);
        trainerIds.add(trainer3);
    }

    @And("the trainee with ID {string} is assigned to trainer with ID {string}")
    public void traineeIsAssignedToTrainer(String traineeId, String trainerId) {
        PageImpl<TrainersTraineeResponse> page = new PageImpl<>(Collections.singletonList(
                new TrainersTraineeResponse(trainerId, "Trainer One", "Lastname One", 1L)));
        when(traineeService.getTrainersNotInTrainersTraineeListByTraineeUserUsername(any(), any())).thenReturn(page);
    }

    @When("the trainee with ID {string} tries to retrieve trainers not assigned to her")
    public void traineeRetrievesTrainersNotAssigned(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        if (!traineeId.equals(id)) {
            doThrow(new ProfileNotFoundException("Trainee not found")).when(traineeService).getTrainersNotInTrainersTraineeListByTraineeUserUsername(any(), any());
        }
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/not-assigned-trainers")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "trainerId,asc"))
                .andReturn();
    }

    @When("the trainee with ID {string} tries to retrieve trainers not assigned to her but the trainee has not logged in before")
    public void traineeRetrievesTrainersNotAssignedButHasNotLoggedInBefore(String id) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        doThrow(new ForbidenException("Unauthorized access")).when(traineeService).getTrainersNotInTrainersTraineeListByTraineeUserUsername(any(), any());
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/not-assigned-trainers"))
                .andReturn();
    }

    @And("the response should contain trainers with IDs {string}, {string}")
    public void responseShouldContainTrainers(String trainer1, String trainer2) throws Exception {
        String json = result.getResponse().getContentAsString();
        PagedModel<EntityModel<TrainersTraineeResponse>> pagedModel =
                objectMapper.readValue(
                        json,
                        new TypeReference<PagedModel<EntityModel<TrainersTraineeResponse>>>() {}
                );

        List<TrainersTraineeResponse> trainers = pagedModel.getContent().stream()
                .map(EntityModel::getContent)
                .toList();

        Assertions.assertTrue(
                trainers.stream().anyMatch(t -> t.getTrainerUsername().equals(trainer1)),
                "Response should contain trainer ID: " + trainer1
        );
        Assertions.assertTrue(
                trainers.stream().anyMatch(t -> t.getTrainerUsername().equals(trainer2)),
                "Response should contain trainer ID: " + trainer2
        );
    }

    @And("the response should not contain trainer with ID {string}")
    public void responseShouldNotContainTrainer(String trainerId) throws Exception {
        String json = result.getResponse().getContentAsString();

        PagedModel<EntityModel<TrainersTraineeResponse>> pagedModel =
                objectMapper.readValue(
                        json,
                        new TypeReference<PagedModel<EntityModel<TrainersTraineeResponse>>>() {}
                );

        List<TrainersTraineeResponse> trainers = pagedModel.getContent().stream()
                .map(EntityModel::getContent)
                .toList();

        Assertions.assertFalse(
                trainers.stream().anyMatch(t -> t.getTrainerUsername().equals(trainerId)),
                "Response should not contain trainer ID: " + trainerId
        );
    }


    // --- Get Trainings For Trainee ---
    @And("trainings with IDs {string}, {string} exist and are assigned to trainee with ID {string}")
    public void trainingsWithIdsExistAndAssignedToTrainee(String training1, String training2, String traineeId) {
        List<TrainingTraineesResponse> trainings = new ArrayList<>();
        trainings.add(new TrainingTraineesResponse(training1, new Date(), "Yoga", 1.0, "Training One"));
        trainings.add(new TrainingTraineesResponse(training2, new Date(), "Yoga", 2.0, "Training Two"));
        PageImpl<TrainingTraineesResponse> page = new PageImpl<>(trainings);
        when(trainingService.getTraineeTrainings(any(), any(), any(), any(), any(), any())).thenReturn(page);
    }

    @When("the trainee with ID {string} retrieves their trainings")
    public void traineeRetrievesTrainings(String id) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        if (!traineeId.equals(id)) {
            doThrow(new ProfileNotFoundException("Trainee not found")).when(trainingService).getTraineeTrainings(any(), any(), any(), any(), any(), any());
        }
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/trainings")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "trainingId,asc"))
                .andReturn();
    }

    @When("the trainee with ID {string} retrieves their trainings but the trainee has not logged in before")
    public void traineeRetrievesTrainingsButHasNotLoggedInBefore(String id) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        doThrow(new ForbidenException("Unauthorized access")).when(trainingService).getTraineeTrainings(any(), any(), any(), any(), any(), any());
        result = mockMvc.perform(MockMvcRequestBuilders.get("/trainee/profile/" + id + "/trainings"))
                .andReturn();
    }

    // --- Update Trainee ---

    @When("the trainee with ID {string} tries to update the trainee information for ID {string} with first name {string}, last name {string}, birthdate {isoDate}, and address {string}")
    public void traineeUpdatesProfile(String id, String targetId, String firstName, String lastName, Date birthdate, String address) throws Exception {
        isAuthenticated = true;
        isAuthorized = true;
        traineeUpdateRequest = new TraineeUpdateRequest();
        traineeUpdateRequest.setFirstName(firstName);
        traineeUpdateRequest.setLastName(lastName);
        traineeUpdateRequest.setDateOfBirth(birthdate);
        traineeUpdateRequest.setAddress(address);
        traineeUpdateRequest.setIsActive(true);
        traineeUpdateRequest.setUsername(targetId);

        TraineeUpdateResponse response = new TraineeUpdateResponse(targetId, firstName, lastName, birthdate, address, true, new HashSet<>());
        if (!traineeId.equals(targetId)) {
            doThrow(new UserNotFoundException("Trainee not found")).when(traineeService).updateTrainee(any(), any());
        } else {
            when(traineeService.updateTrainee(any(), any())).thenReturn(response);
        }
        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(traineeUpdateRequest)))
                .andReturn();
    }

    @When("the trainee with ID {string} tries to update the trainee information for ID {string} with first name {string}, last name {string}, birthdate {isoDate}, and address {string} but the trainee has not logged in before")
    public void traineeUpdatesProfileButHasNotLoggedInBefore(String id, String targetId, String firstName, String lastName, Date birthdate, String address) throws Exception {
        isAuthenticated = false;
        isAuthorized = false;
        traineeUpdateRequest.setFirstName(firstName);
        traineeUpdateRequest.setLastName(lastName);
        traineeUpdateRequest.setDateOfBirth(birthdate);
        traineeUpdateRequest.setAddress(address);
        traineeUpdateRequest.setIsActive(true);
        traineeUpdateRequest.setUsername(targetId);

        doThrow(new ForbidenException("Unauthorized access")).when(traineeService).updateTrainee(any(), any());

        result = mockMvc.perform(MockMvcRequestBuilders.put("/trainee/profile/" + targetId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traineeUpdateRequest)))
                .andReturn();
    }

    @And("the trainee information should be updated to first name {string}, last name {string}, birthdate {isoDate}, and address {string}")
    public void traineeInformationShouldBeUpdated(String firstName, String lastName, Date birthdate, String address) throws Exception {
        String json = result.getResponse().getContentAsString();
        EntityModel<TraineeUpdateResponse> entityModel = objectMapper.readValue(
                json,
                new TypeReference<EntityModel<TraineeUpdateResponse>>() {}
        );
        TraineeUpdateResponse response = entityModel.getContent();
        Assertions.assertNotNull(response, "Response content should not be null");
        Assertions.assertEquals(firstName, response.getFirstName(), "First name should match");
        Assertions.assertEquals(lastName, response.getLastName(), "Last name should match");
        Assertions.assertEquals(birthdate, response.getDateOfBirth(), "Birthdate should match");
        Assertions.assertEquals(address, response.getAddress(), "Address should match");
    }

}
