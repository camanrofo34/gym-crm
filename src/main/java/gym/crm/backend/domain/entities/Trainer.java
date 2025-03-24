package gym.crm.backend.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "trainer")
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Setter
    private User user;

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Setter
    private Set<Training> trainings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization", nullable = false)
    @Setter
    private TrainingType specialization;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainer_trainee",
            joinColumns = @JoinColumn(name = "trainer_id"),
            inverseJoinColumns = @JoinColumn(name = "trainee_id")
    )
    @Setter
    private Set<Trainee> trainees;
}
