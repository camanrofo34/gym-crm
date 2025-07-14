package gym.crm.backend.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import gym.crm.backend.domain.entities.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class TrainerWorkloadRequest {

    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;

    private Double trainingDuration;

    private ActionType actionType;

}
