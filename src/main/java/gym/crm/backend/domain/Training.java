package gym.crm.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "training")
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trainingName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date trainingDate;

    @Column(nullable = false)
    private Double trainingDuration;

    @ManyToOne(optional = false)
    @JoinColumn(name = "traineeId", referencedColumnName = "id")
    private Trainee trainee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainerId", referencedColumnName = "id")
    private Trainer trainer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "trainingTypeId", referencedColumnName = "id")
    private TrainingType trainingType;
}
