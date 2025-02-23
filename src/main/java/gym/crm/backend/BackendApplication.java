package gym.crm.backend;

import gym.crm.backend.configuration.AppConfig;
import gym.crm.backend.facade.Facade;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

		Facade facade = context.getBean(Facade.class);
		facade.run();

		context.close();
	}

}
