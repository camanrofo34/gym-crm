package gym.crm.hours_microservice.domain.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MonthlyWorkload {

    @Setter
    @NotBlank
    private String trainingMonth;

    @Setter
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double totalHours;
}


