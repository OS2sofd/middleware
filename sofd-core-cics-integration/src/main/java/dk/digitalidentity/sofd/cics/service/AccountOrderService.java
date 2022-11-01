package dk.digitalidentity.sofd.cics.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.sofd.cics.dao.model.Municipality;
import dk.digitalidentity.sofd.cics.service.model.AccountOrder;
import dk.digitalidentity.sofd.cics.service.model.AccountOrderStatus;
import dk.digitalidentity.sofd.cics.service.model.Person;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountOrderService {

	@Autowired
	private KspCicsService kspCicsService;
	
	@Autowired
	private SOFDService sofdService;

	public void execute(Municipality municipality) {
		try {
			List<AccountOrder> orders = sofdService.getPendingOrders(municipality);
			List<AccountOrderStatus> result = new ArrayList<>();
			
			if (orders.size() == 0) {
				return;
			}
			
			log.info("Handling " + orders.size() + " orders for " + municipality.getName());

			for (AccountOrder order : orders) {
				
				// fetch person details for orders of type CREATE
				if ("CREATE".equals(order.getOrderType())) {
					try {
						Collection<Person> personResult = sofdService.getPersons(order.getPerson().getCpr(), municipality);
						if (personResult == null || personResult.isEmpty()) {
							throw new Exception("No person with uuid " + order.getPerson().getUuid() + " in SOFD for " + municipality.getName());
						}

						Person person = personResult.iterator().next();
						order.getPerson().setPerson(person);
					}
					catch (Exception ex) {
						order.setStatus("FAILED");
						order.setMessage(ex.getMessage());
						continue;
					}
				}
				
				switch (order.getOrderType()) {
					case "DEACTIVATE":
					case "DELETE":
						deleteUser(municipality, order);
						break;
					case "CREATE":
						createUser(municipality, order);
						break;
					default:
						order.setStatus("FAILED");
						order.setMessage("Ukendt ordretype: " + order.getOrderType());
						log.error("Unknown order type: " + order.getOrderType());
						break;
				}
			}
			
			for (AccountOrder order : orders) {
				AccountOrderStatus status = new AccountOrderStatus();
				status.setAffectedUserId(order.getUserId());
				status.setId(order.getId());
				status.setMessage(order.getMessage());
				status.setStatus(order.getStatus());
				
				result.add(status);
			}
		
			sofdService.setStatusOnOrders(municipality, result);
		}
		catch (Exception ex) {
			log.error("Failed to handle account orders for " + municipality.getName(), ex);
		}
	}

	private void createUser(Municipality municipality, AccountOrder order) {
		String result = kspCicsService.createUser(municipality, order.getPerson().getPerson(), order.getUserId());
		if (result != null) {
			order.setStatus("FAILED");
			order.setMessage(result);
		}
		else {
			order.setStatus("CREATED");
		}
	}

	private void deleteUser(Municipality municipality, AccountOrder order) {
		String result = kspCicsService.deleteUser(municipality, order.getUserId());
		if (result != null) {
			order.setStatus("FAILED");
			order.setMessage(result);
		}
		else {
			switch (order.getOrderType()) {
				case "DEACTIVATE":
					order.setStatus("DEACTIVATED");
					break;
				case "DELETE":
					order.setStatus("DELETED");
					break;
				default:
					log.error("Unknown order type: " + order.getOrderType());
					break;
			}
		}
	}
}
