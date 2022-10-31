package dk.sofd.organization.ad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dk.sofd")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
