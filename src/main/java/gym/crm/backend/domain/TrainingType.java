package gym.crm.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "training_type")
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, updatable = false)
    @Setter
    private TrainingTypes trainingTypeName;

    @OneToMany(mappedBy = "trainingType", fetch = FetchType.LAZY)
    @Setter
    private List<Training> trainings;

    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    @Setter
    private List<Trainer> trainers;
}
