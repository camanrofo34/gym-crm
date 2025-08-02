package gym.crm.hours_microservice.domain.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "trainer_workload_summary")
@CompoundIndex(name = "first_last_name_index", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
public class TrainerWorkloadSummary {

    @Id
    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean trainerStatus;

    private List<@Valid YearlyWorkload> yearlyWorkloads = new ArrayList<>();
}


