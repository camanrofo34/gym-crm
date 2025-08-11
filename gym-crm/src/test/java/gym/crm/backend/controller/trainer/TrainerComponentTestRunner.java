package gym.crm.backend.controller.trainer;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/controller/trainer",
        glue = "gym/crm/backend/controller/trainer",
        plugin = {"pretty"}
)
public class TrainerComponentTestRunner {
}
