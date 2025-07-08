package gym.crm.backend.domain.representationModel;

import gym.crm.backend.domain.response.trainingType.TrainingTypeResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.lang.NonNull;

public class TrainingTypeResponseAssembler implements RepresentationModelAssembler<TrainingTypeResponse,
        EntityModel<TrainingTypeResponse>> {

    @Override
    @NonNull
    public EntityModel<TrainingTypeResponse> toModel(@NonNull TrainingTypeResponse entity) {
        return EntityModel.of(entity);
    }
}
