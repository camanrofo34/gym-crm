package gym.crm.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

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
    private Set<Training> trainings;

    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    @Setter
    private Set<Trainer> trainers;
}
