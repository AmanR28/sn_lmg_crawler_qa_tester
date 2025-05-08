package com.lmg.crawler_qa_tester.service.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmg.crawler_qa_tester.exception.ComparatorException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Log4j2
public class ApiService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonNode config;

    public ApiService() throws IOException {
        config = mapper.readTree(new File("config.json"));
    }

    public String getApiUrl(String env, String country, String concept, String lang, String apiType) {
        JsonNode envData = config.path("envConfig").path(env.toLowerCase());
        if (envData.isMissingNode()) {
            throw new ComparatorException("envConfig", "Environment " + env + " not found in the config.", HttpStatus.NOT_FOUND);
        }
        log.info("Resolved environment: {}", env);
        String subDomain = envData.path("subdomain").asText();
        JsonNode countryData = envData.path(country.toLowerCase());
        if (countryData.isMissingNode()) {
            throw new ComparatorException("Country", "Country " + country + " not found in the config.", HttpStatus.NOT_FOUND);
        }
        log.info("Resolved country: {}", country);

        JsonNode conceptData = countryData.path(concept.toLowerCase());
        if (conceptData.isMissingNode()) {
            throw new ComparatorException("Concept", "Concept " + concept + " not found in the config.", HttpStatus.NOT_FOUND);
        }
        log.info("Resolved concept: {}", concept);
        JsonNode api = conceptData.path(apiType);
        String fetchId = api.path("id").asText();
        String app = api.path("app").asText();
        String domainSuffix = api.path("domainSuffix").asText().toLowerCase();

        String endpoint = switch (apiType) {
            case HEADER_STRIP_API_NAME -> HEADER_STRIP_API_URL_SUFFIX;
            case HEADER_NAV_API_NAME -> HEADER_NAV_API_URL_SUFFIX;
            case FOOTER_STRIP_API_NAME -> FOOTER_API_URL_SUFFIX;
            default -> throw new ComparatorException("API", "Unknown API type: " + apiType, HttpStatus.NOT_FOUND);
        };

        String url = String.format(
                "https://%s.%s%s?id=%s&app=%s&l=%s",
                subDomain.toLowerCase(), domainSuffix, endpoint, fetchId, app, lang.toLowerCase()
        );

        log.info("Generated API URL: {}", url);
        return url;
    }

    public JsonNode callApi(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response == null) {
            log.info("Http Response Null");
            throw new ComparatorException("NULL", "Http Response Null for -" + url, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (response.statusCode() != HttpStatus.OK.value()) {
            log.info("Http status - {}", response.statusCode());
            throw new ComparatorException("Http status:"+ response.statusCode() +" for - " + url, response.body(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return mapper.readTree(response.body());
    }
}