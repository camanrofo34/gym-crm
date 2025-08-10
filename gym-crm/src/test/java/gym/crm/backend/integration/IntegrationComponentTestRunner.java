package gym.crm.backend.integration;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/integration",
        glue = "gym/crm/backend/integration",
        plugin = {"pretty"}
)
public class IntegrationComponentTestRunner {
}
