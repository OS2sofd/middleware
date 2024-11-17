package dk.sofd.opus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.autoconfigure.cache.ElastiCacheAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.jdbc.AmazonRdsDatabaseAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;

@SpringBootApplication(scanBasePackages = "dk.sofd")
@EnableAutoConfiguration(exclude = {
		ContextInstanceDataAutoConfiguration.class,
        ElastiCacheAutoConfiguration.class,
        ContextStackAutoConfiguration.class,
        AmazonRdsDatabaseAutoConfiguration.class,
        MessagingAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
