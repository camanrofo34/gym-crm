package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Trainee extends User{
    private Long traineeId;
    private String dateOfBirth;
    private String address;

    public Trainee(String firstName, String lastName, String dateOfBirth, String address) {
        super(firstName, lastName, true);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}
