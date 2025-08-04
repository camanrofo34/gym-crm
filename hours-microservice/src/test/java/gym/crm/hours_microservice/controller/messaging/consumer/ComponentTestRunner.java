package gym.crm.hours_microservice.controller.messaging.consumer;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/messaging.consumer",
        glue = "gym/crm/hours_microservice/controller/messaging/consumer",
        plugin = {"pretty"}
)
public class ComponentTestRunner {
}

