package gym.crm.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "training")
public class Training {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String trainingName;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    @Setter
    private Date trainingDate;

    @Column(nullable = false)
    @Setter
    private Double trainingDuration;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainee_id")
    @Setter
    private Trainee trainee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trainer_id")
    @Setter
    private Trainer trainer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_type_id")
    @Setter
    private TrainingType trainingType;

}
