package gym.crm.hours_microservice.service;

import gym.crm.hours_microservice.domain.entity.ActionType;
import gym.crm.hours_microservice.domain.entity.MonthlyWorkload;
import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import gym.crm.hours_microservice.domain.entity.YearlyWorkload;
import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.repository.MonthlyWorkloadRepository;
import gym.crm.hours_microservice.repository.TrainerWorkloadSummaryRepository;
import gym.crm.hours_microservice.repository.YearlyWorkloadRepository;
import gym.crm.hours_microservice.service.implementation.TrainerWorkloadServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TrainerWorkloadServiceImpl.class)
class TrainerWorkloadServiceTest {

    @Autowired
    private TrainerWorkloadServiceImpl service;

    @Autowired
    private TrainerWorkloadSummaryRepository summaryRepository;

    @Autowired
    private YearlyWorkloadRepository yearlyRepository;

    @Autowired
    private MonthlyWorkloadRepository monthlyRepository;

    private final ZoneId zoneId = ZoneId.systemDefault();

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

        service.updateTrainerWorkload(request);

        Optional<TrainerWorkloadSummary> summaryOpt = summaryRepository.findById("john.doe");
        assertThat(summaryOpt).isPresent();

        Optional<YearlyWorkload> yearly = yearlyRepository.findByTrainerTrainerUsernameAndTrainingYear("john.doe", year);
        assertThat(yearly).isPresent();

        Optional<MonthlyWorkload> monthly = monthlyRepository.findByYearlyWorkloadIdAndTrainignMonth(yearly.get().getId(), month);
        assertThat(monthly).isPresent();
        assertThat(monthly.get().getTotalHours()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("Should accumulate training duration for the same trainer and month")
    void accumulateTrainingDurationForSameTrainerAndMonth() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest first = new TrainerWorkloadRequest("jane.smith", "Jane", "Smith", true, now, 1.0, ActionType.ADD);
        TrainerWorkloadRequest second = new TrainerWorkloadRequest("jane.smith", "Jane", "Smith", true, now, 3.0, ActionType.ADD);

        service.updateTrainerWorkload(first);
        service.updateTrainerWorkload(second);

        Optional<YearlyWorkload> yearly = yearlyRepository.findByTrainerTrainerUsernameAndTrainingYear("jane.smith", year);
        assertThat(yearly).isPresent();

        Optional<MonthlyWorkload> monthly = monthlyRepository.findByYearlyWorkloadIdAndTrainignMonth(yearly.get().getId(), month);
        assertThat(monthly).isPresent();
        assertThat(monthly.get().getTotalHours()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Should subtract training duration and remove month if duration becomes zero or less")
    void subtractTrainingDurationAndRemoveMonthIfZeroOrLess() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest add = new TrainerWorkloadRequest("alex.lee", "Alex", "Lee", true, now, 2.0, ActionType.ADD);
        TrainerWorkloadRequest delete = new TrainerWorkloadRequest("alex.lee", "Alex", "Lee", true, now, 2.0, ActionType.DELETE);

        service.updateTrainerWorkload(add);
        service.updateTrainerWorkload(delete);

        Optional<YearlyWorkload> yearly = yearlyRepository.findByTrainerTrainerUsernameAndTrainingYear("alex.lee", year);
        assertThat(yearly).isPresent();
        Optional<MonthlyWorkload> monthly = monthlyRepository.findByYearlyWorkloadIdAndTrainignMonth(yearly.get().getId(), month);
        assertThat(monthly).isEmpty();
    }

    @Test
    @DisplayName("Should subtract training duration and update month if duration remains positive")
    void subtractTrainingDurationAndUpdateMonthIfPositive() {
        Date now = new Date();
        String year = String.valueOf(now.toInstant().atZone(zoneId).toLocalDate().getYear());
        String month = String.format("%02d", now.toInstant().atZone(zoneId).toLocalDate().getMonthValue());

        TrainerWorkloadRequest add = new TrainerWorkloadRequest("maria.garcia", "Maria", "Garcia", true, now, 5.0, ActionType.ADD);
        TrainerWorkloadRequest delete = new TrainerWorkloadRequest("maria.garcia", "Maria", "Garcia", true, now, 2.0, ActionType.DELETE);

        service.updateTrainerWorkload(add);
        service.updateTrainerWorkload(delete);

        Optional<YearlyWorkload> yearly = yearlyRepository.findByTrainerTrainerUsernameAndTrainingYear("maria.garcia", year);
        assertThat(yearly).isPresent();

        Optional<MonthlyWorkload> monthly = monthlyRepository.findByYearlyWorkloadIdAndTrainignMonth(yearly.get().getId(), month);
        assertThat(monthly).isPresent();
        assertThat(monthly.get().getTotalHours()).isEqualTo(3.0);
    }

}