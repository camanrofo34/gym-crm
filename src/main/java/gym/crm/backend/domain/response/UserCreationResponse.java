package gym.crm.backend.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserCreationResponse {
    private String username;
    private String password;
}
