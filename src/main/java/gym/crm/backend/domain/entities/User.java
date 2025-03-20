package gym.crm.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private String firstName;

    @Column(nullable = false)
    @Setter
    private String lastName;

    @Column(nullable = false, unique = true)
    @Setter
    private String username;

    @Column(nullable = false)
    @Setter
    private String password;

    @Column(nullable = false)
    @Setter
    private Boolean isActive = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @Setter
    private Trainer trainer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @Setter
    private Trainee trainee;
}
