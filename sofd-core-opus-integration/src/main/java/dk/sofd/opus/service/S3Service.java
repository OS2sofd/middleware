package dk.sofd.opus.service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.sofd.opus.config.MunicipalityConfiguration;
import dk.sofd.opus.dao.model.Municipality;
import lombok.SneakyThrows;

@Service
public class S3Service {
	
    @Autowired
    private ResourcePatternResolver resourcePatternResolver;
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MunicipalityConfiguration configuration;

    public String getNewestFilename(Municipality municipality, String filePrefix) {
    	if (municipality.isUseS3FileShare()) {
    		return getNewestFileNameThroughS3(municipality, filePrefix);
    	}
    	else {
    		return getNewestFileNameDirect(municipality, filePrefix);
    	}
    }

    private String getNewestFileNameThroughS3(Municipality municipality, String filePrefix) {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getS3FileShareApiKey()));
		
		ResponseEntity<String[]> response = restTemplate.exchange(configuration.getS3FileshareUrl() + "/files/list", HttpMethod.GET, request, String[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException("Failed to fetch OPUS files from s3fileshare. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		String[] files = response.getBody();
		
        String newestFileName = null;
        for (String file : files) {
            if (!file.startsWith(filePrefix)) {
            	continue;
            }
            
            if (newestFileName == null || file.compareTo(newestFileName) > 0) {
            	newestFileName = file;
            }
        }

        return newestFileName;
	}

	@SneakyThrows
    private String getNewestFileNameDirect(Municipality municipality, String filePrefix) {    	
    	// try the opus subfolder first - if it is empty, try the root
        Resource[] xmlFiles = resourcePatternResolver.getResources("s3://" + municipality.getBucket() + "/opus/" + filePrefix + "*");
        Resource newestResource = null;
        for (Resource resource : xmlFiles) {
            if (newestResource == null || resource.getFilename().compareTo(newestResource.getFilename()) > 0) {
                newestResource = resource;
            }
        }
        
        if (newestResource != null) {
        	return newestResource.getFilename();
        }
        
        // fallback to root folder
        xmlFiles = resourcePatternResolver.getResources("s3://" + municipality.getBucket() + "/" + filePrefix + "*");
        newestResource = null;
        for (Resource resource : xmlFiles) {
            if (newestResource == null || resource.getFilename().compareTo(newestResource.getFilename()) > 0) {
                newestResource = resource;
            }
        }

        return newestResource == null ? null : newestResource.getFilename();
    }

	@SneakyThrows
    public InputStreamReader readFile(Municipality municipality, String fileName) {
    	if (municipality.isUseS3FileShare()) {
    		return readFileThroughS3(municipality, fileName);
    	}
    	else {
    		return readFileDirect(municipality, fileName);
    	}
    }
    
    private InputStreamReader readFileThroughS3(Municipality municipality, String fileName) throws Exception {
		HttpEntity<String> request = new HttpEntity<>(getHeaders(municipality.getS3FileShareApiKey()));
		
		ResponseEntity<byte[]> response = restTemplate.exchange(configuration.getS3FileshareUrl() + "/files?file=" + fileName, HttpMethod.GET, request, byte[].class);
		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			throw new RuntimeException("Failed to fetch OPUS file from s3fileshare. " + response.getStatusCodeValue() + ", response=" + response.getBody());
		}

		return new InputStreamReader(new ByteArrayInputStream(response.getBody()));
	}

	@SneakyThrows
    private InputStreamReader readFileDirect(Municipality municipality, String fileName) {
		Resource resource = resourcePatternResolver.getResource("s3://" + municipality.getBucket() + "/" + fileName);

		return new InputStreamReader(resource.getInputStream());
    }
	
	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);

		return headers;
	}
}