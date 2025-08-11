package gym.crm.backend.controller.trainee;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/controller/trainee",
        glue = "gym/crm/backend/controller/trainee",
        plugin = {"pretty"}
)
public class TraineeComponentTestRunner {
}
