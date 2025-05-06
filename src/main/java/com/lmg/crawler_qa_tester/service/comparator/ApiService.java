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
        JsonNode envData = config.path("envConfig").path(env.toUpperCase());

        if (envData.isMissingNode()) {
            throw new ComparatorException("envConfig", "Environment " + env + " not found in the config.", HttpStatus.NOT_FOUND);
        }
        log.info("Resolved environment: {}", env);

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
            case HEADER_NAV_API_NAME -> HEADER_STRIP_API_URL_SUFFIX;
            case FOOTER_STRIP_API_NAME -> HEADER_STRIP_API_URL_SUFFIX;
            default -> throw new ComparatorException("API", "Unknown API type: " + apiType, HttpStatus.NOT_FOUND);
        };

        String url = String.format(
                "https://%s.%s%s?id=%s&app=%s&l=%s",
                env.toLowerCase(), domainSuffix, endpoint, fetchId, app, lang.toLowerCase()
        );

        log.info("Generated API URL: {}", url);
        return url;
    }

    public JsonNode callApi(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }
}