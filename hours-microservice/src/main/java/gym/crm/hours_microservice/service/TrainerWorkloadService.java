package gym.crm.hours_microservice.service;

import gym.crm.hours_microservice.domain.request.TrainerWorkloadRequest;

public interface TrainerWorkloadService {

    void updateTrainerWorkload(TrainerWorkloadRequest trainerWorkloadRequest);
}
