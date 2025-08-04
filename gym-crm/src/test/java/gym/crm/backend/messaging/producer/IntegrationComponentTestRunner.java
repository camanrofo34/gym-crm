package gym.crm.backend.messaging.producer;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/messaging/producer",
        glue = "gym/crm/backend/messaging/producer",
        plugin = {"pretty"}
)
public class IntegrationComponentTestRunner {
}
