package gym.crm.backend.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class Trainee extends User{
    private Long traineeId;
    @Setter
    private String dateOfBirth;
    @Setter
    private String address;

    public Trainee(String firstName, String lastName, String dateOfBirth, String address) {
        super(firstName, lastName, true);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public Trainee(long traineeId, String firstName, String lastName, String dateOfBirth, String address){
        super(firstName, lastName, true);
        this.traineeId = traineeId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}
