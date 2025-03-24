package gym.crm.backend.domain.representationModel;

import gym.crm.backend.domain.response.training.TrainingTraineesResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


@Component
public class TrainingTraineesResponseAssembler implements RepresentationModelAssembler<TrainingTraineesResponse,
        EntityModel<TrainingTraineesResponse>> {

    @Override
    @NonNull
    public EntityModel<TrainingTraineesResponse> toModel(@NonNull TrainingTraineesResponse entity) {
        return EntityModel.of(entity);
    }

}
