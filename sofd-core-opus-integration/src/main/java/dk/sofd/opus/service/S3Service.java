package dk.sofd.opus.service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import dk.sofd.opus.config.MunicipalityConfiguration;
import dk.sofd.opus.dao.model.Municipality;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class S3Service {
    private AmazonS3 amazonS3;

    @Autowired
    private AWSCredentialsProvider credentialsProvider;

    @Value("${s3.endpoint:https://s3.amazonaws.com}")
    private String endpoint;

    @Value("${s3.region:eu-west-1}")
    private String region;

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
        try {
            ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(municipality.getBucket()).withPrefix(filePrefix);
            ListObjectsV2Result result;

            List<S3ObjectSummary> summaries = new ArrayList<S3ObjectSummary>();
            do {
                try {
                    result = getS3Client().listObjectsV2(request);
                }
                catch(Exception e) {
                    log.warn("Call to Amazon failed resetting AmazonClient");
                    amazonS3 = null;
                    throw(e);
                }

                summaries.addAll(result.getObjectSummaries());
                request.setContinuationToken(result.getNextContinuationToken());
            } while (result.isTruncated());

            S3ObjectSummary newestSummary = null;
            for (var summary : summaries) {
                if (newestSummary == null || summary.getLastModified().after(newestSummary.getLastModified())) {
                	newestSummary = summary;
                }

            }
            
            return newestSummary == null ? null : newestSummary.getKey();
        } 
        catch (Exception ex) {
            log.warn("Call to Amazon failed resetting AmazonClient");
            amazonS3 = null;

        	log.error("getNewestFileNameDirect operation failed for " + municipality.getName(), ex);
            return null;
        }
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
        GetObjectRequest getObjectRequest = new GetObjectRequest(municipality.getBucket(), fileName);
        S3Object s3Object;

        try {
            s3Object = getS3Client().getObject(getObjectRequest);
        }
        catch(Exception e) {
            log.warn("Call to Amazon failed resetting AmazonClient");
            amazonS3 = null;
            throw(e);
        }

		return new InputStreamReader(s3Object.getObjectContent());
    }
	
	private HttpHeaders getHeaders(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("apiKey", apiKey);

		return headers;
	}
	
    private AmazonS3 getS3Client() {
        if (amazonS3 == null) {
            amazonS3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();
        }

        return amazonS3;
    }
}