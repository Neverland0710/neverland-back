package projcet.neverland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = "projcet.neverland",
		exclude = { SecurityAutoConfiguration.class }
)
public class NeverlandApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeverlandApplication.class, args);
	}
}