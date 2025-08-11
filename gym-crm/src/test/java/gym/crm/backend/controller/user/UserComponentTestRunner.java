package gym.crm.backend.controller.user;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/controller/user",
        glue = "gym/crm/backend/controller/user",
        plugin = {"pretty"}
)
public class UserComponentTestRunner {
}
