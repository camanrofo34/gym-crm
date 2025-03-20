package gym.crm.backend.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TrainerUpdateRequest {

    @NotBlank(message = "Firstname cannot be blank")
    private String firstName;

    @NotBlank(message = "Lastname cannot be blank")
    private String lastName;

    @NotNull(message = "Date of birth cannot be blank")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Long trainingTypeId;

    @NotBlank(message = "Is Active cannot be blank")
    private Boolean isActive;
}
