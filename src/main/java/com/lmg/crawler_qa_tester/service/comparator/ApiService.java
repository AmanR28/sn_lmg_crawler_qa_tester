package com.lmg.crawler_qa_tester.service.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmg.crawler_qa_tester.exception.ComparatorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Slf4j
public final class ApiService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper mapper;
    private final JsonNode config;

    public ApiService() throws IOException {
        this.mapper = new ObjectMapper();
        this.config = mapper.readTree(new File("config.json"));
    }

    public String getApiUrl(String env, String country, String concept, String lang, String apiType) {
        Objects.requireNonNull(env, "Environment cannot be null");
        Objects.requireNonNull(country, "Country cannot be null");
        Objects.requireNonNull(concept, "Concept cannot be null");
        Objects.requireNonNull(apiType, "API type cannot be null");

        final var envLower = env.toLowerCase();
        final var countryLower = country.toLowerCase();
        final var conceptLower = concept.toLowerCase();

        // Get configuration nodes with validation
        final var envData = getConfigNode(config.path("envConfig"), envLower, "Environment");
        final var countryData = getConfigNode(envData, countryLower, "Country");
        final var conceptData = getConfigNode(countryData, conceptLower, "Concept");
        final var apiConfig = getConfigNode(conceptData, apiType, "API");

        log.info("Resolved configuration - Env: {}, Country: {}, Concept: {}, API: {}",
                env, country, concept, apiType);

        return switch (apiType) {
            case LEFT_HEADER_STRIP_API_NAME -> buildLeftHeaderStripUrl(apiConfig, conceptLower);
            default -> buildStandardApiUrl(envData.path("subdomain").asText(),
                    apiConfig,
                    lang != null ? lang.toLowerCase() : LANG_EN_CODE,
                    apiType);
        };
    }

    private JsonNode getConfigNode(JsonNode parentNode, String key, String nodeType) {
        final var node = parentNode.path(key);
        if (node.isMissingNode()) {
            throw new ComparatorException(nodeType, "%s %s not found in config".formatted(nodeType, key),
                    HttpStatus.NOT_FOUND);
        }
        return node;
    }

    private String buildLeftHeaderStripUrl(JsonNode apiConfig, String conceptLower) {
        final var url = "https://%slive.%s%s%s?depth=%s&format=%s".formatted(
                conceptLower,
                apiConfig.path("domainSuffix").asText().toLowerCase(),
                LEFT_HEADER_STRIP_API_URL_SUFFIX,
                apiConfig.path("id").asText(),
                apiConfig.path("depth").asText(),
                apiConfig.path("format").asText()
        );

        log.debug("Built Left Header Strip URL: {}", url);
        return url;
    }
    public  String getProdApiUrl(String env, String country, String concept)
    {
        try{
            return config.path("envConfig")
                    .path(env)
                    .path(country)
                    .path(concept)
                    .path("apiUrl")
                    .asText();
        }
        catch(Exception e)
        {
            return null;
        }


    }

    private String buildStandardApiUrl(String subdomain, JsonNode apiConfig, String lang, String apiType) {
        final var endpoint = switch (apiType) {
            case RIGHT_HEADER_STRIP_API_NAME -> RIGHT_HEADER_STRIP_API_URL_SUFFIX;
            case HEADER_NAV_API_NAME -> HEADER_NAV_API_URL_SUFFIX;
            case FOOTER_STRIP_API_NAME -> FOOTER_API_URL_SUFFIX;
            default -> throw new ComparatorException("API", "Unsupported API type: " + apiType,
                    HttpStatus.BAD_REQUEST);
        };

        final var url = "https://%s.%s%s?id=%s&app=%s&l=%s".formatted(
                subdomain.toLowerCase(),
                apiConfig.path("domainSuffix").asText().toLowerCase(),
                endpoint,
                apiConfig.path("id").asText(),
                apiConfig.path("app").asText(),
                lang
        );

        log.debug("Built Standard API URL for {}: {}", apiType, url);
        return url;
    }

    public JsonNode callApi(String url) throws IOException, InterruptedException {
        Objects.requireNonNull(url, "URL cannot be null");

        final var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        final var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != HttpStatus.OK.value()) {
            log.error("API call failed - Status: {}, URL: {}, Response: {}",
                    response.statusCode(), url, response.body());
            throw new ComparatorException(
                    "API call failed with status %d".formatted(response.statusCode()),
                    response.body(),
                    HttpStatus.valueOf(response.statusCode())
            );
        }

        return mapper.readTree(response.body());
    }

//    public String getProdApiUrl(String env, String country, String concept, String lang) {
//        String domainSuffix = "maxfashion.com";
//        String appCountryCode = concept.toLowerCase() + country.toLowerCase(); // e.g., maxkw
//
//        return String.format("https://www.%s/landmarkshopscommercews/cache/v3/%s/%s/cmsNavigationData" +
//                "?pageId=%s-Homepage&appId=Desktop&fields=FULL&position=HEADER,NAVIGATION,FOOTER",
//                domainSuffix, appCountryCode, lang.toLowerCase(), appCountryCode);
//    }
}