package dk.digitalidentity.service.nexus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;

import dk.digitalidentity.dao.model.Municipality;
import dk.digitalidentity.dao.model.User;
import dk.digitalidentity.service.WebClientBuilderService;
import dk.digitalidentity.service.nexus.model.Autosignature;
import dk.digitalidentity.service.nexus.model.Change;
import dk.digitalidentity.service.nexus.model.DefaultOrganizationSupplier;
import dk.digitalidentity.service.nexus.model.Employee;
import dk.digitalidentity.service.nexus.model.EmployeeConfiguration;
import dk.digitalidentity.service.nexus.model.FMKRoleConfiguration;
import dk.digitalidentity.service.nexus.model.MunicipalityTrust;
import dk.digitalidentity.service.nexus.model.NexusTimeoutException;
import dk.digitalidentity.service.nexus.model.OU;
import dk.digitalidentity.service.nexus.model.PreferencesConfiguration;
import dk.digitalidentity.service.nexus.model.ProfessionalJob;
import dk.digitalidentity.service.sofd.model.Person;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.PrematureCloseException;

@Slf4j
@Component
@EnableCaching
public class NexusStub {
	private Map<String, Long> networkErrorCounter = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private WebClientBuilderService webClientBuilderService;

    @CacheEvict(value = "autosignatures", allEntries = true)
    public void cleanUpAutosignatures() {
    	;
    }
    
    @CacheEvict(value = "organizationSuppliers", allEntries = true)
    public void cleanUpOrganizationSuppliers() {
    	;
    }

    @CacheEvict(value = "professionalJobs", allEntries = true)
    public void cleanProfessionalJobs() {
    	;
    }
    
    @CacheEvict(value = "flatNexusOUs", allEntries = true)
    public void cleanupFlatNexusOUs() {
    	;
    }

    @Cacheable("organizationSuppliers")
    public List<DefaultOrganizationSupplier> getOrganizationSuppliers(Municipality municipality) {
        List<DefaultOrganizationSupplier> result = new ArrayList<>();
        var url = municipality.getNexusBaseUrl() + "/suppliers/organization";
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);

        try {
            var response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            var dto = mapper.readValue(response, DefaultOrganizationSupplier[].class);

            result = Arrays.asList(dto);
            
            clearTimeout(municipality);
        }
        catch (Exception e) {
        	if (e instanceof WebClientRequestException ex && ex.contains(ReadTimeoutException.class)) {
        		if (criticalTimeout(municipality)) {
                    log.error("Failed to fetch list of DefaultOrganizationSuppliers from Nexus - timeout for " + municipality.getName(), e);        			
        		}
        		else {
        			log.warn("Failed to fetch list of DefaultOrganizationSuppliers from Nexus - timeout for " + municipality.getName());
        		}
        	}
        	else {
        		log.error("Failed to fetch list of DefaultOrganizationSuppliers from Nexus - unknown error for " + municipality.getName(), e);
        	}
        }

        return result;
    }

	@Cacheable("professionalJobs")
    public List<ProfessionalJob> getProfessionalJobs(Municipality municipality) {
        List<ProfessionalJob> result = new ArrayList<>();
        var url = municipality.getNexusBaseUrl() + "/professionalJobs";
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);

        try {
            var response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            var dto = mapper.readValue(response, ProfessionalJob[].class);
            
            result = Arrays.asList(dto);
            
            clearTimeout(municipality);
        }
        catch (Exception e) {            
            if (e instanceof WebClientRequestException ex && ex.contains(ReadTimeoutException.class)) {
        		if (criticalTimeout(municipality)) {
                    log.error("Failed to fetch list of ProfessionalJob from Nexus - timeout for " + municipality.getName(), e);        			
        		}
        		else {
        			log.warn("Failed to fetch list of ProfessionalJob from Nexus - timeout for " + municipality.getName());
        		}
        	}
        	else {
                log.error("Failed to fetch list of ProfessionalJob from Nexus for " + municipality.getName(), e);
        	}
        }

        return result;
    }

    @Cacheable("autosignatures")
    public List<Autosignature> getAutosignatures(Municipality municipality) {
        List<Autosignature> result = new ArrayList<>();

        try {
            WebClient client = webClientBuilderService.getNexusWebClient(municipality);
            var url = municipality.getNexusBaseUrl() + "/professionalAutosignatures/active";

            var response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            clearTimeout(municipality);

            return Arrays.asList(mapper.readValue(response, Autosignature[].class));
        }
        catch (Exception e) {
        	if (e instanceof WebClientRequestException ex && ex.contains(ReadTimeoutException.class)) {
        		if (criticalTimeout(municipality)) {
                    log.error("Failed to fetch Nexus Autosignatures for - timeout for " + municipality.getName(), e);        			
        		}
        		else {
        			log.warn("Failed to fetch Nexus Autosignatures for - timeout for " + municipality.getName());
        		}
        	}
        	else {
                log.error("Failed to fetch Nexus Autosignatures for " + municipality.getName(), e);
        	}
        }
        
        return result;
    }
    
    public String createEmployeeFromStsPrototype(Municipality municipality, String url, Employee prototype, Person person, String kombitUuid) {
        String response = null;

        try {
	        WebClient client = webClientBuilderService.getNexusWebClient(municipality);

	        response = client.post()
	                .uri(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .bodyValue(prototype)
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
        }
        catch (WebClientResponseException ex) {
        	String body = ex.getResponseBodyAsString();
        	
        	if (body != null && (body.contains("ProfessionalServiceDataIntegrity") || body.contains("ProfessionalServiceDuplicateProfessionalWhenTryingToCreate"))) {
        		ObjectMapper mapper = new ObjectMapper();
        		String payload = null;
        		try {
        			payload = mapper.writeValueAsString(prototype);
        		}
        		catch (Exception ignored) {
        			;
        		}

        		log.warn(municipality.getName() + " : Brugeren findes i forvejen - fx en bruger med samme unikId, initialer, STS UUID eller lignende. SOFD UUID = " + kombitUuid + ", full response = " + body);
        		log.warn(municipality.getName() + " : payload = " + payload);
        		
        		// 0 means exists already
        		return "0";
        	}
        	else if (body != null && body.contains("ProfessionalServiceProfessionalInvalidState")) {
        		ObjectMapper mapper = new ObjectMapper();
        		String payload = null;
        		try {
        			payload = mapper.writeValueAsString(prototype);
        		}
        		catch (Exception ignored) {
        			;
        		}

        		log.warn(municipality.getName() + " : Brugeren har dårlige data, enten i kaldet eller ovre i FK Organisation, full response = " + body);
        		log.warn(municipality.getName() + " : payload = " + payload);
        		
        		// 1 means bad data
        		return "1";
        	}

        	// I suspect that when FK Organisation does not work, it throws some empty HTTP 500, but this text is contained currently,
        	// so we can filter it out - it might change in the future, so this has a time-to-live as a supression for alarms :)
        	if (body != null && body.contains("DefaultResponseErrorHandler.java:111")) {
        		log.warn("Failed to create employee, message=" + ex.getResponseBodyAsString(), ex);
        	}
        	else {
        		log.error("Failed to create employee, message=" + ex.getResponseBodyAsString(), ex);
        	}
       		
    		try {
    			log.info("request: " + mapper.writeValueAsString(prototype));
    		}
    		catch (Exception exx) {
    			;
    		}
        }
        catch (WebClientRequestException ex) {
        	if (ex.contains(ReadTimeoutException.class)) {
        		log.warn("Timeout on creating user: " + ex.getMessage());
        		return null;
        	}
        	
        	throw ex;
        }
        
        return response;
    }
    
    record ConflictDetails(long id) { }
    record ConflictBrokenObject(ConflictDetails details) { }
    record ConflictResponse(ConflictBrokenObject brokenObject) { }

    enum EmployeePrototypeStatus { OK, OTHER_WITH_SAME_CPR_EXISTS, TECHNICAL_ERROR, FAILED }
    record EmployeeWrapper(Employee employee, EmployeePrototypeStatus status, long existingEmployeeId) { }
    public EmployeeWrapper getEmployeePrototype(Municipality municipality, String uuid) throws JsonProcessingException {
    	try {
	        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
	        var url = municipality.getNexusBaseUrl() + "/professionals/stsProfessional/prototype?query=" + uuid;

	        var response = client.get()
	                .uri(url)
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
	        
	        return new EmployeeWrapper(mapper.readValue(response, Employee.class), EmployeePrototypeStatus.OK, 0);
    	}
    	catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();

			if (ex.getRawStatusCode() == 400) {
    			if (body != null) {
    				if (body.contains("ProfessionalWithStsSnNotFetched")) {
    					// some unknown FK Organisation error
    					log.warn(municipality.getName() + " : got ProfessionalWithStsSnNotFetched Nexus error on call to FK Organisation on " + uuid);
    					
    					// failed is a retry
    	    	        return new EmployeeWrapper(null, EmployeePrototypeStatus.FAILED, 0);
    				}
    			}
			}
			else if (ex.getRawStatusCode() == 409) {
				long id = 0;
    			if (body != null) {
    				ConflictResponse response = mapper.readValue(body, ConflictResponse.class);
    				if (response != null && response.brokenObject() != null && response.brokenObject().details() != null) {
    					id = response.brokenObject().details().id();
    				}
    				
    				if (body.contains("ProfessionalWithSameCprAlreadyExists")) {
    	    	        return new EmployeeWrapper(null, EmployeePrototypeStatus.OTHER_WITH_SAME_CPR_EXISTS, id);
    				}
    			}
    		}
			else if (ex.getRawStatusCode() == 500) {
    			if (body != null) {
    				if (body.contains("UnsupportedOperationException")) {
    					log.warn(municipality.getName() + " : got UnsupportedOperationException on creating " + uuid);
    					
    					// technical error is permanent
    	    	        return new EmployeeWrapper(null, EmployeePrototypeStatus.TECHNICAL_ERROR, 0);
    				}
    				else if (body.contains("http://fk-organizations/")) {
    					log.warn(municipality.getName() + " : got internal Nexus timeout on call to FK Organisation on " + uuid);
    					
    					// failed is a retry
    	    	        return new EmployeeWrapper(null, EmployeePrototypeStatus.FAILED, 0);    					
    				}
    				// some sort of internal null pointer exception - probably because of an error in FK Org lookup, so we will attempt retry
    				else if (body.contains("500 null")) {
    					log.warn(municipality.getName() + " : got NPE from Nexus - probably FK Org " + uuid);
    					
    					// failed is a retry
    	    	        return new EmployeeWrapper(null, EmployeePrototypeStatus.FAILED, 0);    					
    				}
    			}
    		}

    		log.error(municipality.getName() + " : Failed to fetch prototype for user with uuid " + uuid + ", HTTP " + ex.getRawStatusCode() + ", message=" + body, ex);
    		return new EmployeeWrapper(null, EmployeePrototypeStatus.FAILED, 0);
    	}
    	catch (WebClientRequestException ex) {
    		if (ex.contains(ReadTimeoutException.class)) {
        		log.warn(municipality.getName() + " : timeout on getEmployeePrototype", ex);
        		return new EmployeeWrapper(null, EmployeePrototypeStatus.FAILED, 0);
    		}
    		
    		throw ex;
    	}
    }
    
    @Cacheable("flatNexusOUs")
    public List<OU> getOusFlat(Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        String url = municipality.getNexusBaseUrl() + "/organizations/tree";
        OU rootOU = null;
        
        try {
            String response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            rootOU = mapper.readValue(response, OU.class);
        }
        catch (JsonProcessingException e) {
            log.error(municipality.getName() + " : Could not parse response to OU");
            return null;
        }
    	catch (WebClientRequestException ex) {
    		if (ex.contains(ReadTimeoutException.class)) {
        		log.warn(municipality.getName() + " : timeout on getOusFlat", ex);
        		return null;
    		}
    		
    		throw ex;
    	}

        List<OU> flat = new ArrayList<>();
        flattenTree(rootOU, flat);
        
        // filter inactive
        flat.removeIf(ou -> !ou.isActive());

        return flat;
    }
    
    public boolean changeAssignedOus(Change change, long nexusId, Municipality municipality){
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        String url = municipality.getNexusBaseUrl() + "/professionals/" + nexusId + "/organizations/changes";
        
        try {
	        client.post()
	                .uri(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .bodyValue(change)
	                .retrieve()
	                .toBodilessEntity()
	                .block();
	        
	        return true;
        }
        catch (WebClientRequestException ex) {
        	if (ex.contains(ReadTimeoutException.class)) {
        		throw new NexusTimeoutException("Timeout on changeAssignedOus", ex);
        	}
        }
        catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
	        log.error("Failed to change organizations for employee with Nexus id " +  nexusId + ", message = " + body, ex);
	        
    		try {
    			log.info("request: " + mapper.writeValueAsString(change));
    		}
    		catch (Exception exx) {
    			;
    		}
        }

        return false;
    }
    
    public List<OU> getUserOrgUnits(User user, Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/" + user.getNexusId() + "/organizations";
    
        OU[] dto = null;
        try {
	        var response = client.get()
	                .uri(url)
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
		        
	        try {
	            dto = mapper.readValue(response, OU[].class);
	        }
	        catch (JsonProcessingException ex) {
	            log.error("Exception when trying to parse get organizations result when getting for user with userId " + user.getUserId() + " in Nexus", ex);
	            return null;
	        }
        }
        catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();
        	log.error("Failed to get orgUnits for " + user.getUserId() + ", message=" + body, ex);

        	return null;
        }
        catch (WebClientRequestException ex) {
        	if (ex.contains(ReadTimeoutException.class)) {
        		throw new NexusTimeoutException("Timeout on getUserOrgUnits", ex);
        	}
        	else if (ex.contains(PrematureCloseException.class)) {
        		throw new NexusTimeoutException("PrematureClose on getUserOrgUnits", ex);
        	}
        	
        	throw ex;
        }

        return Arrays.asList(dto);
    }
    
    // TODO: duplicate of above method
    public List<OU> getOusForEmployee(long id, Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        String url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/organizations";
        OU[] ous = null;
        
        try {
            String response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            ous = mapper.readValue(response, OU[].class);
        }
        catch (JsonProcessingException e) {
            log.error("Could not parse response to OU");
            return new ArrayList<>();
        }

        return Arrays.asList(ous);
    }

    public List<OU> getMedcomOus(Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        String url = municipality.getNexusBaseUrl() + "/organizations/withEAN";
        OU[] ous = null;
        
        try {
            String response = client.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            ous = mapper.readValue(response, OU[].class);
        }
        catch (JsonProcessingException e) {
            log.error("Could not parse response to medcom OU");
            return new ArrayList<>();
        }

        return Arrays.asList(ous);
    }

    // output might be truncated (per Employee), so trust the following fields only
    // * id
    // * primaryIdentifier
    // * active
	public Employee[] getEmployees(Municipality municipality) throws URISyntaxException {
		WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = new URI(municipality.getNexusBaseUrl() + "/professionals?query=%00");
        
        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
        	return mapper.readValue(response, Employee[].class);
        }
        catch (JsonProcessingException ex) {
        	log.error("failed to process employees", ex);
        }
        
        return null;
	}
    
	public Employee getEmployeeByUuid(Municipality municipality, String kombitUuid, String userId) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals?query=" + kombitUuid;
        
        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
        	Employee[] employees = mapper.readValue(response, Employee[].class);
        	
        	if (employees.length > 0) {
        		for (Employee employee : employees) {
        			if (employee.getPrimaryIdentifier() == null) {
        				continue;
        			}

	        		if (Objects.equal(employee.getPrimaryIdentifier().toLowerCase(), userId.toLowerCase())) {
	        			return employee;
	        		}
        		}
        	}
        }
        catch (JsonProcessingException ex) {
        	log.error(municipality.getName() + " : failed to process employee result for search on uuid " + kombitUuid, ex);
        }
        
        return null;
	}
	
    public Employee getFullEmployee(long id, Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id;
        
        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
        	return mapper.readValue(response, Employee.class);
        }
        catch (JsonProcessingException ex) {
        	log.error("failed to process employee " + id, ex);
        }
        
        return null;
    }
    
    public EmployeeConfiguration getEmployeeConfiguration(long id, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/configuration/" + id;
 
        String response = null;
        try {
	        response = client.get()
	                .uri(url)
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
	        
	        return mapper.readValue(response, EmployeeConfiguration.class);
        }
        catch (JsonMappingException ex) {
        	// for debugging purposes - this happens a lot in review, and it would be nice to know why
        	log.warn(municipality.getName() + ": Failed to parse employeeConfiguration for (" + id + "): " + response);
        	
        	throw ex;
        }
        catch (WebClientRequestException ex) {
        	if (ex.contains(ReadTimeoutException.class)) {
        		throw new NexusTimeoutException("Timeout on getEmployeeConfiguration", ex);
        	}
        	else if (ex.contains(PrematureCloseException.class)) {
        		throw new NexusTimeoutException("PrematureClose on getEmployeeConfiguration", ex);
        	}
        	
        	throw ex;
        }
    }

    public MunicipalityTrust getMunicipalityTrust(long id, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/municipalityTrust";

        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return mapper.readValue(response, MunicipalityTrust.class);
    }

	public boolean setMunicipalityTrust(long id, Municipality municipality, boolean trusted) {
		MunicipalityTrust municipalityTrust = new MunicipalityTrust();
		municipalityTrust.setTrusted(trusted);
		WebClient client = webClientBuilderService.getNexusWebClient(municipality);
		var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/municipalityTrust";

		try {
			client.put()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(municipalityTrust)
				.retrieve()
				.bodyToMono(String.class)
				.block();

			return true;
		}
		catch (WebClientRequestException ex) {
			if (ex.contains(ReadTimeoutException.class)) {
				throw new NexusTimeoutException(municipality.getName() + " : Timeout on setting trust on id = " + id, ex);
			}
		}
		catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();

			log.error(municipality.getName() + " : Failed to update municipality trust (payload in next logline) for employee with id " + id + " with response=" + body, ex);

			try {
				log.warn("failed request payload: " + mapper.writeValueAsString(municipalityTrust));
			}
			catch (Exception e) {
				;
			}
		}
		return false;
	}

	public enum UpdateResult { OK, FAILED, FAILED_DELETE_FROM_USER_TABLE }
    public UpdateResult updateEmployeeConfiguration(EmployeeConfiguration employeeConfiguration, Municipality municipality) {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/configuration/" + employeeConfiguration.getId();

        try {
	        client.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(employeeConfiguration)
                .retrieve()
                .bodyToMono(String.class)
                .block();

	        return UpdateResult.OK;
        }
        catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();

			boolean duplicateError = false;
			if (ex.getRawStatusCode() == 500) {
				// update rejected because one of the various unique fields overlaps another existing user - this _should_ fix itself
				// after some updates (when the other user is updated, it should no longer conflict, and then THIS can be updated)
				if (body != null && body.contains("Duplicate entry")) {
					duplicateError = true;
					log.warn(municipality.getName() + " : Failed to update employeeConfiguration " + employeeConfiguration.getId() + " because of a potential overlap with another user - message from Nexus: " + body);
				}
			}
			else if (ex.getRawStatusCode() == 400) {
				// update rejected because CPR exists on multiple users
				if (body != null && body.contains("ProfessionalServiceMultipleProfessionalReturnedByCPR")) {
					duplicateError = true;
					log.warn(municipality.getName() + " : Failed to update " + employeeConfiguration.getPrimaryIdentifier() + " because of error ProfessionalServiceMultipleProfessionalReturnedByCPR");
				}
				// update rejected because another user has the same primaryIdentifier
				else if (body != null && body.contains("ProfessionalServiceDataIntegrity") && body.contains("brokenObjectString=" + employeeConfiguration.getPrimaryIdentifier())) {
					duplicateError = true;
					log.warn(municipality.getName() + " : failed to update + " + employeeConfiguration.getPrimaryIdentifier() + " because of error ProfessionalServiceDataIntegrity");
					
					return UpdateResult.FAILED_DELETE_FROM_USER_TABLE;
				}
			}

			if (!duplicateError) {
	        	log.error(municipality.getName() + " : Failed to call updateEmployeeConfiguration (payload in next logline) with response=" + body, ex);
	
	        	try {
	        		log.warn(municipality + " : failed request payload: " + mapper.writeValueAsString(employeeConfiguration));
	        	}
	        	catch (Exception e) {
	        		;
	        	}
			}
        }
        
        return UpdateResult.FAILED;
    }
    
    public FMKRoleConfiguration updateFMKRole(long id, FMKRoleConfiguration fmkRoleConfiguration, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/fmkRole";
        var response = client.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(fmkRoleConfiguration)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return mapper.readValue(response, FMKRoleConfiguration.class);
    }

    public FMKRoleConfiguration getFMKRole(long id, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/fmkRole";
        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return mapper.readValue(response, FMKRoleConfiguration.class);
    }
    
    public boolean updateEmployee(Employee employee, Municipality municipality) {
    	try {
	        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
	        var url = municipality.getNexusBaseUrl() + "/professionals/" + employee.getId();

	        client.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(employee)
                .retrieve()
                .bodyToMono(String.class)
                .block();
	        
	        return true;
    	}
    	catch (WebClientResponseException ex) {
			String body = ex.getResponseBodyAsString();

			boolean duplicateError = false;
			if (ex.getRawStatusCode() == 500) {
				// update rejected because one of the various unique fields overlaps another existing user - this _should_ fix itself
				// after some updates (when the other user is updated, it should no longer conflict, and then THIS can be updated)
				if (body != null && body.contains("Duplicate entry")) {
					duplicateError = true;
					log.warn(municipality.getName() + " : Failed to update employee " + employee.getId() + " because of a potential overlap with another user - message from Nexus: " + body);
				}
			}

			if (!duplicateError) {
	    		log.error("Failed to update employee with id " + employee.getId() + ", message=" + body, ex);
	    		
	        	try {
	        		log.warn("failed request payload: " + mapper.writeValueAsString(employee));
	        	}
	        	catch (Exception e) {
	        		;
	        	}
			}
    	}

    	return false;
    }

    public PreferencesConfiguration getPreferencesConfiguration(long id, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/preferences/DEFAULT_PAGE/default";
        
        var response = client.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        return mapper.readValue(response, PreferencesConfiguration.class);
    }

    public PreferencesConfiguration createPreferencesConfiguration(long id, PreferencesConfiguration preferencesConfiguration, Municipality municipality) throws JsonProcessingException {
        WebClient client = webClientBuilderService.getNexusWebClient(municipality);
        var url = municipality.getNexusBaseUrl() + "/professionals/" + id + "/preferences/DEFAULT_PAGE/default";
        
        try {
	        var response = client.post()
	                .uri(url)
	                .contentType(MediaType.APPLICATION_JSON)
	                .bodyValue(preferencesConfiguration)
	                .retrieve()
	                .bodyToMono(String.class)
	                .block();
	        
	        return mapper.readValue(response, PreferencesConfiguration.class);
        }
        catch (WebClientRequestException ex) {
        	if (ex.contains(ReadTimeoutException.class)) {
        		throw new NexusTimeoutException("Timeout on createPreferencesConfiguration", ex);
        	}
        	
        	throw ex;
        }
    }
    
	private void flattenTree(OU root, List<OU> result) {
		result.add(root);

		for (OU child : root.getChildren()) {
			flattenTree(child, result);
		}
	}
	
    private boolean criticalTimeout(Municipality municipality) {
    	Long counter = 1L;

    	if (networkErrorCounter.containsKey(municipality.getCvr())) {
    		counter = networkErrorCounter.get(municipality.getCvr()) + 1;
    	}
    	
    	networkErrorCounter.put(municipality.getCvr(), counter);
    	
    	return counter > 5;
	}
    
    private void clearTimeout(Municipality municipality) {
    	networkErrorCounter.remove(municipality.getCvr());
    }
}
