package gym.crm.hours_microservice.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class YearlyWorkload {

    @Setter
    @NotBlank
    private String trainingYear;

    private List<@Valid MonthlyWorkload> monthlyWorkloads = new ArrayList<>();
}


