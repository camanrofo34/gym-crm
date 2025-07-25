package gym.crm.hours_microservice.service.implementation;

import gym.crm.hours_microservice.domain.entity.ActionType;
import gym.crm.hours_microservice.domain.entity.MonthlyWorkload;
import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import gym.crm.hours_microservice.domain.entity.YearlyWorkload;
import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.repository.TrainerWorkloadSummaryRepository;
import gym.crm.hours_microservice.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadSummaryRepository trainerRepo;

    @Override
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String username = request.getTrainerUsername();
        LocalDate localDate = request.getTrainingDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String year = String.valueOf(localDate.getYear());
        String month = String.format("%02d", localDate.getMonthValue());

        TrainerWorkloadSummary trainerWorkloadSummary = trainerRepo.findByTrainerUsername(username).orElseGet(() -> {
            return new TrainerWorkloadSummary(
                    username, request.getTrainerFirstName(),
                    request.getTrainerLastName(), request.getIsActive(), new ArrayList<>()
            );
        });

        YearlyWorkload yearlyWorkload = trainerWorkloadSummary.getYearlyWorkloads().stream().
                filter(y -> y.getTrainingYear().equals(year)).findFirst().
                orElseGet(
                        () -> {
                            YearlyWorkload newYearWorkload = new YearlyWorkload(
                                    year, new ArrayList<>()
                            );
                            trainerWorkloadSummary.getYearlyWorkloads().add(newYearWorkload);
                            return newYearWorkload;
                        }
                );

        MonthlyWorkload monthlyWorkload = yearlyWorkload.getMonthlyWorkloads().stream().
                filter(m -> m.getTrainingMonth().equals(month)).findFirst().
                orElseGet(
                        () -> {
                            MonthlyWorkload newMonthlyWorkload = new MonthlyWorkload(
                                    month, 0.0
                            );
                            yearlyWorkload.getMonthlyWorkloads().add(newMonthlyWorkload);
                            return newMonthlyWorkload;
                        }
                );


        if (request.getActionType() == ActionType.ADD) {
            monthlyWorkload.setTotalHours(monthlyWorkload.getTotalHours() + request.getTrainingDuration());
        } else if (request.getActionType() == ActionType.DELETE) {
            double updated = monthlyWorkload.getTotalHours() - request.getTrainingDuration();
            if (updated <= 0) {
                yearlyWorkload.getMonthlyWorkloads().remove(monthlyWorkload);
            } else {
                monthlyWorkload.setTotalHours(updated);
            }
        }

        trainerRepo.save(trainerWorkloadSummary);
    }
}

