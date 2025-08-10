package gym.crm.backend.controller.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import gym.crm.backend.controller.UserController;
import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.exception.handler.GlobalExceptionHandler;
import gym.crm.backend.exception.types.forbidden.ForbidenException;
import gym.crm.backend.service.JwtService;
import gym.crm.backend.service.NotAllowedTokenListService;
import gym.crm.backend.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@CucumberContextConfiguration
@AutoConfigureMockMvc
public class StepDefinitions {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotAllowedTokenListService notAllowedTokenListService;

    @InjectMocks
    private UserController userController;

    private LoginRequest loginRequest;
    private LoginRequest originalUser;
    private String token;
    private MvcResult result;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, passwordEncoder, jwtService, notAllowedTokenListService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new GlobalExceptionHandler()).build();
        loginRequest = new LoginRequest();
        originalUser = new LoginRequest();
    }

    @Given("a user with username {string} and password {string}")
    public void theUserProvidesValidLoginCredentials(String username, String password) {
        originalUser.setUsername(username);
        originalUser.setPassword(password);
    }

    @When("the user attempts to login with username {string} and password {string}")
    public void theUserAttemptsToLoginWithUsernameAndPassword(String username, String password) throws Exception {
        loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        if (originalUser.getUsername().equals(username) && originalUser.getPassword().equals(password)) {
            System.out.println("Simulating successful login for user: " + username);
            when(userService.login(any(LoginRequest.class))).thenReturn(true);
            when(jwtService.generateToken(username)).thenReturn("mocked-token");
        } else {
            doThrow(new ForbidenException("Unauthorized access")).when(userService).login(any());
        }
        result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
    }

    @Then("a JWT token should be returned")
    public void aJWTTokenShouldBeReturned() throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains("mocked-token"));
    }

    @Given("a user with username {string} and password {string} is authenticated")
    public void aUserWithUsernameAndPasswordIsAuthenticated(String username, String password) {
        originalUser.setUsername(username);
        originalUser.setPassword(password);
    }

    @When("the user attempts to change the password with old password {string} and new password {string}")
    public void theUserAttemptsToChangeThePasswordWithOldPasswordAndNewPassword(String oldPassword, String newPassword) throws Exception {

        if (originalUser.getPassword().equals(oldPassword)) {
            doNothing().when(userService).changePassword(any(), any());
        } else {
            doThrow(new ForbidenException("Old password is incorrect")).when(userService).changePassword(any(), any());
        }

        result = mockMvc.perform(MockMvcRequestBuilders.put("/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(originalUser))
                        .param("newPassword", newPassword))
                .andReturn();

    }

    @Then("the password change response status should be {int}")
    public void thePasswordChangeResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @When("the user attempts to logout")
    public void theUserAttemptsToLogout() throws Exception {
        String transactionId = "mocked-transaction-id";
        doNothing().when(notAllowedTokenListService).notAllowToken(Mockito.anyString());

        result = mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer mocked-token")
                        .header("Transaction-Id", transactionId))
                .andReturn();
    }

    @Then("the logout response status should be {int}")
    public void theLogoutResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @And("the token should be added to the blacklist")
    public void theTokenShouldBeAddedToTheBlacklist() {
        String token = result.getResponse().getHeader("Authorization");
        Assertions.assertNull(token);
    }

    @When("the user attempts to login with username {string} and password {string} six times")
    public void theUserAttemptsToLoginWithUsernameAndPasswordSixTimes(String username, String password) throws Exception {
        for (int i = 0; i < 6; i++) {
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);

            if (originalUser.getUsername().equals(username) && originalUser.getPassword().equals(password)) {
                when(userService.login(any(LoginRequest.class))).thenReturn(true);
                when(jwtService.generateToken(username)).thenReturn("mocked-token");
            } else {
                when(userService.login(any(LoginRequest.class))).thenReturn(false);
            }

            result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andReturn();
        }
    }

    @Then("the login response status should be {int}")
    public void theLoginResponseStatusShouldBe(int status) {
        Assertions.assertEquals(status, result.getResponse().getStatus());
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String message) throws Exception {
        String responseContent = result.getResponse().getContentAsString();
        Assertions.assertTrue(responseContent.contains(message), "Response should contain: " + message);
    }


    @When("the user attempts to change the password with old password {string} and new password {string} but the user has not logged in before")
    public void theUserAttemptsToChangeThePasswordWithOldPasswordAndNewPasswordButTheUserHasNotLoggedInBefore(String oldPassword, String newPassword) throws Exception {
        originalUser.setUsername("nonexistentUser");
        originalUser.setPassword(oldPassword);

        doThrow(new ForbidenException("Unauthorized access")).when(userService).changePassword(any(), any());

        result = mockMvc.perform(MockMvcRequestBuilders.put("/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(objectMapper.writeValueAsString(originalUser))
                        .param("newPassword", newPassword))
                .andReturn();
    }
}

