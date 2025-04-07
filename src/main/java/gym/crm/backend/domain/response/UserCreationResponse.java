package gym.crm.backend.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserCreationResponse {
    private String username;
    private String password;
}
