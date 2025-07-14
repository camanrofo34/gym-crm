package gym.crm.hours_microservice.service.implementation;

import gym.crm.hours_microservice.domain.entity.ActionType;
import gym.crm.hours_microservice.domain.entity.MonthlyWorkload;
import gym.crm.hours_microservice.domain.entity.TrainerWorkloadSummary;
import gym.crm.hours_microservice.domain.entity.YearlyWorkload;
import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;
import gym.crm.hours_microservice.repository.MonthlyWorkloadRepository;
import gym.crm.hours_microservice.repository.TrainerWorkloadSummaryRepository;
import gym.crm.hours_microservice.repository.YearlyWorkloadRepository;
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
    private final YearlyWorkloadRepository yearRepo;
    private final MonthlyWorkloadRepository monthRepo;

    @Override
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String username = request.getTrainerUsername();
        LocalDate localDate = request.getTrainingDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String year = String.valueOf(localDate.getYear());
        String month = String.format("%02d", localDate.getMonthValue());

        TrainerWorkloadSummary trainer = trainerRepo.findById(username).orElseGet(() -> {
            TrainerWorkloadSummary t = new TrainerWorkloadSummary(username, request.getTrainerFirstName(),
                    request.getTrainerLastName(), request.getIsActive(), new ArrayList<>());
            return trainerRepo.save(t);
        });

        YearlyWorkload yearly = yearRepo.findByTrainerTrainerUsernameAndTrainingYear(username, year)
                .orElseGet(() -> {
                    YearlyWorkload y = new YearlyWorkload();
                    y.setTrainingYear(year);
                    y.setTrainer(trainer);
                    return yearRepo.save(y);
                });

        MonthlyWorkload monthly = monthRepo.findByYearlyWorkloadIdAndTrainignMonth(yearly.getId(), month)
                .orElseGet(() -> {
                    MonthlyWorkload m = new MonthlyWorkload();
                    m.setTrainignMonth(month);
                    m.setTotalHours(0.0);
                    m.setYearlyWorkload(yearly);
                    return m;
                });

        if (request.getActionType() == ActionType.ADD) {
            monthly.setTotalHours(monthly.getTotalHours() + request.getTrainingDuration());
            monthRepo.save(monthly);
        } else if (request.getActionType() == ActionType.DELETE) {
            double updated = monthly.getTotalHours() - request.getTrainingDuration();
            if (updated <= 0) {
                monthRepo.delete(monthly);
            } else {
                monthly.setTotalHours(updated);
                monthRepo.save(monthly);
            }
        }
    }
}
