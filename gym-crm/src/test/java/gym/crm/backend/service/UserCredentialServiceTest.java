package gym.crm.backend.service;

import gym.crm.backend.domain.entities.User;
import gym.crm.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserCredentialServiceTest {

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserCredentialService userCredentialService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateUsername_ShouldReturnUniqueUsername() {
        String firstName = "John";
        String lastName = "Doe";

        when(userRepository.findAll()).thenReturn(List.of(
                new User(1L, "John", "Doe", "John.Doe", "password", true, null, null),
                new User(2L, "Jane", "Doe", "Jane.Doe", "password", true, null, null),
                new User(3L, "John", "Doe", "John.Doe1", "password", true, null, null)
        ));

        String username = userCredentialService.generateUsername(firstName, lastName);

        assertEquals("John.Doe2", username);
    }

    @Test
    void generatePassword_ShouldReturnPasswordOfCorrectLength() {
        String password = userCredentialService.generatePassword();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

}