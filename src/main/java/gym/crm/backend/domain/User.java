package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Setter
public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private boolean isActive;

    public User(String firstName, String lastName, boolean isActive) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
    }
}
