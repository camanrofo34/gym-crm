package gym.crm.backend.service;

import gym.crm.backend.domain.entities.User;
import gym.crm.backend.domain.request.LoginRequest;
import gym.crm.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        boolean result = userService.login(loginRequest);

        assertTrue(result);
    }

    @Test
    void login_Failure_WrongPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("wrongpassword");
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        boolean result = userService.login(loginRequest);

        assertFalse(result);
    }

    @Test
    void login_Failure_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("password123");
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("password1234");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        boolean result = userService.login(loginRequest);

        assertFalse(result);
    }

    @Test
    void changePassword_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("oldpassword");
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("oldpassword");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        boolean result = userService.changePassword(loginRequest, "newpassword");

        assertTrue(result);
        assertEquals("newpassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_Failure_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("oldpassword");
        when(userRepository.findByUsername(anyString())).thenReturn(null);

        boolean result = userService.changePassword(loginRequest, "newpassword");

        assertFalse(result);
    }

    @Test
    void changePassword_Failure_Exception() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("johndoe");
        loginRequest.setPassword("oldpassword");
        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("oldpassword");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        doThrow(new RuntimeException()).when(userRepository).save(any(User.class));

        boolean result = userService.changePassword(loginRequest, "newpassword");

        assertFalse(result);
    }

    @Test
    void activateDeactivateUser_Success() {
        User user = new User();
        user.setUsername("johndoe");
        user.setIsActive(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        userService.activateDeactivateUser("johndoe", false);

        assertTrue(user.getIsActive());
        verify(userRepository, times(1)).save(user);
    }
}