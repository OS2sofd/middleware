package dk.digitalidentity.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dk.digitalidentity.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Component
@EnableScheduling
@Slf4j
public class DeleteFailedUsersFromCacheTask {

	@Autowired
	private UserService userService;
	
	@Scheduled(cron = "0 #{new java.util.Random().nextInt(55)} 2 * * ?")
	public void deleteFailedUsersFromCache() {
		log.info("Starting deleteFailedUsersFromCache task");

		userService.deleteFailedUsersFromCache();

		log.info("DeleteFailedUsersFromCache task finished");
	}
}
