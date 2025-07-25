package gym.crm.hours_microservice.service;

import gym.crm.hours_microservice.domain.entity.ActionType;
import gym.crm.hours_microservice.domain.entity.MonthlyWorkload;
import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import gym.crm.hours_microservice.domain.entity.YearlyWorkload;
import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.repository.TrainerWorkloadSummaryRepository;
import gym.crm.hours_microservice.service.implementation.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TrainerWorkloadServiceImpl.class)
@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @InjectMocks
    private TrainerWorkloadServiceImpl service;

    @Mock
    private TrainerWorkloadSummaryRepository summaryRepository;

    private final ZoneId zoneId = ZoneId.systemDefault();

    @BeforeEach
    void cleanUp(){
    }

    @Test
    @DisplayName("Should add training duration for a new trainer and month")
    void addTrainingDurationForNewTrainerAndMonth() {
        Date trainingDate = new Date();
        String year = String.valueOf(trainingDate.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", trainingDate.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "john.doe", "John", "Doe", true,
                trainingDate, 2.5, ActionType.ADD
        );

        Mockito.when(summaryRepository.findByTrainerUsername("john.doe")).thenReturn(Optional.empty());

        ArgumentCaptor<TrainerWorkloadSummary> captor = ArgumentCaptor.forClass(TrainerWorkloadSummary.class);
        Mockito.when(summaryRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateTrainerWorkload(request);

        TrainerWorkloadSummary savedSummary = captor.getValue();

        assertThat(savedSummary.getTrainerFirstName()).isEqualTo("John");
        assertThat(savedSummary.getTrainerLastName()).isEqualTo("Doe");
        assertThat(savedSummary.getYearlyWorkloads()).hasSize(1);
        assertThat(savedSummary.getYearlyWorkloads().getFirst().getTrainingYear()).isEqualTo(year);
        assertThat(savedSummary.getYearlyWorkloads().getFirst().getMonthlyWorkloads()).hasSize(1);
        assertThat(savedSummary.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTrainingMonth()).isEqualTo(month);
        assertThat(savedSummary.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTotalHours()).isEqualTo(2.5);
    }


    @Test
    @DisplayName("Should accumulate training duration for the same trainer and month")
    void accumulateTrainingDurationForSameTrainerAndMonth() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest first = new TrainerWorkloadRequest("jane.smith", "Jane", "Smith", true, now, 1.0, ActionType.ADD);
        TrainerWorkloadRequest second = new TrainerWorkloadRequest("jane.smith", "Jane", "Smith", true, now, 3.0, ActionType.ADD);

        TrainerWorkloadSummary existingSummary = new TrainerWorkloadSummary("jane.smith", "Jane", "Smith", true,
                List.of(new YearlyWorkload(year, List.of(new MonthlyWorkload(month, 0.0))))
        );

        Mockito.when(summaryRepository.findByTrainerUsername("jane.smith"))
                .thenReturn(Optional.of(existingSummary));

        ArgumentCaptor<TrainerWorkloadSummary> captor = ArgumentCaptor.forClass(TrainerWorkloadSummary.class);
        Mockito.when(summaryRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateTrainerWorkload(first);
        service.updateTrainerWorkload(second);

        TrainerWorkloadSummary saved = captor.getValue();

        assertThat(saved.getTrainerFirstName()).isEqualTo("Jane");
        assertThat(saved.getTrainerLastName()).isEqualTo("Smith");
        assertThat(saved.getYearlyWorkloads()).hasSize(1);
        assertThat(saved.getYearlyWorkloads().getFirst().getTrainingYear()).isEqualTo(year);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads()).hasSize(1);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTrainingMonth()).isEqualTo(month);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTotalHours()).isEqualTo(4.0);
    }


    @Test
    @DisplayName("Should subtract training duration and remove month if duration becomes zero or less")
    void subtractTrainingDurationAndRemoveMonthIfZeroOrLess() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest add = new TrainerWorkloadRequest("alex.lee", "Alex", "Lee", true, now, 2.0, ActionType.ADD);
        TrainerWorkloadRequest delete = new TrainerWorkloadRequest("alex.lee", "Alex", "Lee", true, now, 2.0, ActionType.DELETE);

        ArrayList<MonthlyWorkload> monthlyWorkload = new ArrayList<>();
        monthlyWorkload.add(new MonthlyWorkload(month, 0.0));
        TrainerWorkloadSummary existingSummary = new TrainerWorkloadSummary("alex.lee", "Alex", "Lee", true,
                List.of(
                        new YearlyWorkload(year, monthlyWorkload)
                )
        );

        Mockito.when(summaryRepository.findByTrainerUsername("alex.lee"))
                .thenReturn(Optional.of(existingSummary));

        ArgumentCaptor<TrainerWorkloadSummary> captor = ArgumentCaptor.forClass(TrainerWorkloadSummary.class);
        Mockito.when(summaryRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateTrainerWorkload(add);
        service.updateTrainerWorkload(delete);

        TrainerWorkloadSummary saved = captor.getValue();

        assertThat(saved.getTrainerFirstName()).isEqualTo("Alex");
        assertThat(saved.getTrainerLastName()).isEqualTo("Lee");
        assertThat(saved.getYearlyWorkloads()).hasSize(1);
        assertThat(saved.getYearlyWorkloads().getFirst().getTrainingYear()).isEqualTo(year);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads()).isEmpty();
    }


    @Test
    @DisplayName("Should subtract training duration and update month if duration remains positive")
    void subtractTrainingDurationAndUpdateMonthIfPositive() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest add = new TrainerWorkloadRequest("maria.garcia", "Maria", "Garcia", true, now, 5.0, ActionType.ADD);
        TrainerWorkloadRequest delete = new TrainerWorkloadRequest("maria.garcia", "Maria", "Garcia", true, now, 2.0, ActionType.DELETE);

        TrainerWorkloadSummary existing = new TrainerWorkloadSummary("maria.garcia", "Maria", "Garcia", true,
                List.of(
                        new YearlyWorkload(year, List.of(new MonthlyWorkload(month, 0.0)))
                )
        );

        Mockito.when(summaryRepository.findByTrainerUsername("maria.garcia"))
                .thenReturn(Optional.of(existing));

        ArgumentCaptor<TrainerWorkloadSummary> captor = ArgumentCaptor.forClass(TrainerWorkloadSummary.class);
        Mockito.when(summaryRepository.save(captor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateTrainerWorkload(add);
        service.updateTrainerWorkload(delete);

        TrainerWorkloadSummary saved = captor.getValue();

        assertThat(saved.getTrainerFirstName()).isEqualTo("Maria");
        assertThat(saved.getTrainerLastName()).isEqualTo("Garcia");
        assertThat(saved.getYearlyWorkloads()).hasSize(1);
        assertThat(saved.getYearlyWorkloads().getFirst().getTrainingYear()).isEqualTo(year);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads()).hasSize(1);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTrainingMonth()).isEqualTo(month);
        assertThat(saved.getYearlyWorkloads().getFirst().getMonthlyWorkloads().getFirst().getTotalHours()).isEqualTo(3.0);
    }
}
