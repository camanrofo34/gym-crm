package gym.crm.backend.domain.representationModel;

import gym.crm.backend.domain.response.trainee.TrainersTraineeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;

public class TrainersTraineeResponseAssembler implements RepresentationModelAssembler<TrainersTraineeResponse,
        EntityModel<TrainersTraineeResponse>> {

    @Override
    @NonNull
    public EntityModel<TrainersTraineeResponse> toModel(@NonNull TrainersTraineeResponse entity) {
        return EntityModel.of(entity);
    }
}
