package com.lmg.crawler_qa_tester.service.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmg.crawler_qa_tester.dto.comparator.ApiEntry;
import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import com.lmg.crawler_qa_tester.exception.ComparatorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompareService {

    private static final Set<String> VALID_LOCALES = Set.of("en", "ar");
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private static final String UNKNOWN_VALUE = "unknown";
    private static final String NULL_VALUE = "null";

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    public ResponseEntity<String> compare(CompareRequest req) {
        try (Workbook wb = new XSSFWorkbook()) {
            // Process standard APIs
            processStandardApis(req, wb);

            // Process left header strip API
            processLeftHeaderStripApi(req, wb);

            String timestamp = LocalDateTime.now().toString().replace(":", "_");
            String fileName = String.format("%s_%s_%s.xlsx", req.getCountry(), req.getConcept(), timestamp);

            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                wb.write(fos);
            }

            log.info("Comparison written to {}", fileName);
            return ResponseEntity.ok("Excel sheet generated, fileName - " + fileName);
        } catch (ComparatorException e) {
            log.error("ComparatorException - {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error during comparison", e);
            throw new ComparatorException("Comparison Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processStandardApis(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        List<ApiEntry> apiEntries = List.of(
                new ApiEntry(RIGHT_HEADER_STRIP_API_NAME, req.getConcept(), req.getCountry(), RIGHT_HEADER_STRIP_SHEET_NAME),
                new ApiEntry(HEADER_NAV_API_NAME, req.getConcept(), req.getCountry(), HEADER_NAV_SHEET_NAME),
                new ApiEntry(FOOTER_STRIP_API_NAME, req.getConcept(), req.getCountry(), FOOTER_STRIP_SHEET_NAME)
        );

        for (ApiEntry entry : apiEntries) {
            for (String lang : LANG_CODES) {
                String url1 = apiService.getApiUrl(req.getFirstEnv(), entry.country(), entry.concept(), lang, entry.name());
                String url2 = apiService.getApiUrl(req.getSecondEnv(), entry.country(), entry.concept(), lang, entry.name());

                JsonNode json1 = apiService.callApi(url1);
                JsonNode json2 = apiService.callApi(url2);

                String sheetName = entry.sheetName() + "_" + lang;
                Sheet sheet = createSheetWithHeader(wb, sheetName, req);

                compareJson(json1, json2, "", sheet);
            }
        }
    }

    private void processLeftHeaderStripApi(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        ApiEntry leftHeaderStripEntry = new ApiEntry(
                LEFT_HEADER_STRIP_API_NAME,
                req.getConcept(),
                req.getCountry(),
                LEFT_HEADER_STRIP_SHEET_NAME
        );

        String url1 = apiService.getApiUrl(req.getFirstEnv(), leftHeaderStripEntry.country(),
                leftHeaderStripEntry.concept(), null, leftHeaderStripEntry.name());
        String url2 = apiService.getApiUrl(req.getSecondEnv(), leftHeaderStripEntry.country(),
                leftHeaderStripEntry.concept(), null, leftHeaderStripEntry.name());

        JsonNode json1 = apiService.callApi(url1);
        JsonNode json2 = apiService.callApi(url2);

        compareJsonLocalized(json1, json2, wb, leftHeaderStripEntry.sheetName(), req);
    }

    private void compareJson(JsonNode node1, JsonNode node2, String parentPath, Sheet sheet) {
        int maxSize = Math.max(
                (node1 != null && node1.isArray()) ? node1.size() : 0,
                (node2 != null && node2.isArray()) ? node2.size() : 0
        );

        for (int i = 0; i < maxSize; i++) {
            JsonNode child1 = safeGet(node1, i);
            JsonNode child2 = safeGet(node2, i);

            JsonNode content1 = getContent(child1);
            JsonNode content2 = getContent(child2);

            String name = getFieldValue(content1, content2, "name");
            String order = getFieldValue(content1, content2, "displayOrder");
            String url1 = getFieldValue(content1, "url");
            String url2 = getFieldValue(content2, "url");

            String currentPath = buildPath(parentPath, name, order);
            writeComparisonRow(sheet, currentPath, url1, url2);

            JsonNode children1 = getChildren(content1);
            JsonNode children2 = getChildren(content2);

            if (hasChildren(children1) || hasChildren(children2)) {
                compareJson(
                        children1 != null ? children1 : objectMapper.createArrayNode(),
                        children2 != null ? children2 : objectMapper.createArrayNode(),
                        currentPath,
                        sheet
                );
            }
        }
    }

    private void compareJsonLocalized(JsonNode json1, JsonNode json2, Workbook wb, String baseName, CompareRequest req) {
        Map<String, Sheet> sheets = new ConcurrentHashMap<>();
        VALID_LOCALES.forEach(locale ->
                sheets.put(locale, getOrCreateSheet(wb, baseName + "_" + locale, req))
        );

        traverseAndCompare(json1, json2, "", sheets);
    }

    private void traverseAndCompare(JsonNode node1, JsonNode node2, String parentPath, Map<String, Sheet> sheets) {
        if (bothNodesNullOrEmpty(node1, node2)) return;

        Set<String> fieldNames = collectFieldNames(node1, node2);

        fieldNames.forEach(field -> {
            JsonNode child1 = safeGet(node1, field);
            JsonNode child2 = safeGet(node2, field);
            String currentPath = buildPath(parentPath, field);

            if ("messages".equals(field)) {
                compareMessagesArray(child1, child2, sheets);
                return;
            }

            if (isContainerNode(child1) || isContainerNode(child2)) {
                traverseAndCompare(child1, child2, currentPath, sheets);
            }
        });
    }

    private void compareMessagesArray(JsonNode messages1, JsonNode messages2, Map<String, Sheet> sheets) {
        int maxSize = Math.max(
                (messages1 != null && messages1.isArray()) ? messages1.size() : 0,
                (messages2 != null && messages2.isArray()) ? messages2.size() : 0
        );

        for (int i = 0; i < maxSize; i++) {
            JsonNode message1 = safeGet(messages1, i);
            JsonNode message2 = safeGet(messages2, i);

            String messageName = getMetaName(message1 != null ? message1 : message2);
            JsonNode description1 = getDescriptionValues(message1);
            JsonNode description2 = getDescriptionValues(message2);

            VALID_LOCALES.forEach(locale -> {
                String val1 = getLocaleValue(description1, locale);
                String val2 = getLocaleValue(description2, locale);
                writeComparisonRow(sheets.get(locale), messageName, val1, val2);
            });
        }
    }

    // Helper methods
    private Sheet createSheetWithHeader(Workbook wb, String name, CompareRequest req) {
        Sheet sheet = wb.createSheet(name);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Path");
        header.createCell(1).setCellValue(req.getFirstEnv());
        header.createCell(2).setCellValue(req.getSecondEnv());
        header.createCell(3).setCellValue("Match");
        return sheet;
    }
    private Sheet getOrCreateSheet(Workbook wb, String name, CompareRequest req) {
        if (wb.getSheet(name) != null ) {
            return wb.getSheet(name);
        }
        Sheet sheet = wb.createSheet(name);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Path");
        header.createCell(1).setCellValue(req.getFirstEnv());
        header.createCell(2).setCellValue(req.getSecondEnv());
        header.createCell(3).setCellValue("Match");
        return sheet;
    }

    private JsonNode safeGet(JsonNode node, int index) {
        return (node != null && index < node.size()) ? node.get(index) : null;
    }

    private JsonNode safeGet(JsonNode node, String field) {
        return node != null ? node.get(field) : null;
    }

    private JsonNode getContent(JsonNode node) {
        return node != null ? node.path("content") : null;
    }

    private JsonNode getChildren(JsonNode node) {
        return node != null ? node.path("children") : null;
    }

    private boolean hasChildren(JsonNode node) {
        return node != null && node.isArray() && node.size() > 0;
    }

    private String getFieldValue(JsonNode node1, JsonNode node2, String field) {
        return Stream.of(node1, node2)
                .filter(Objects::nonNull)
                .filter(n -> n.has(field))
                .findFirst()
                .map(n -> n.path(field).asText(NULL_VALUE))
                .orElse(NULL_VALUE);
    }

    private String getFieldValue(JsonNode node, String field) {
        return (node != null && node.has(field)) ? node.path(field).asText(NULL_VALUE) : NULL_VALUE;
    }

    private String buildPath(String parent, String... parts) {
        String joinedParts = String.join("_", parts);
        return parent.isEmpty() ? joinedParts : parent + "/" + joinedParts;
    }

    private void writeComparisonRow(Sheet sheet, String path, String val1, String val2) {
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(path);
        row.createCell(1).setCellValue(val1);
        row.createCell(2).setCellValue(val2);
        row.createCell(3).setCellValue(val1.equals(val2) ? "YES" : "NO");
    }

    private Set<String> collectFieldNames(JsonNode node1, JsonNode node2) {
        Set<String> fieldNames = new HashSet<>();
        if (node1 != null && node1.isObject()) node1.fieldNames().forEachRemaining(fieldNames::add);
        if (node2 != null && node2.isObject()) node2.fieldNames().forEachRemaining(fieldNames::add);
        return fieldNames;
    }

    private boolean bothNodesNullOrEmpty(JsonNode node1, JsonNode node2) {
        return (node1 == null || node1.isNull() || node1.isEmpty()) &&
                (node2 == null || node2.isNull() || node2.isEmpty());
    }

    private boolean isContainerNode(JsonNode node) {
        return node != null && node.isContainerNode();
    }

    private JsonNode getDescriptionValues(JsonNode messageNode) {
        return messageNode != null ? messageNode.path("description").path("values") : null;
    }

    private String getMetaName(JsonNode messageNode) {
        if (messageNode == null || messageNode.isNull()) return UNKNOWN_VALUE;
        JsonNode meta = messageNode.path("_meta");
        return meta.has("name") ? meta.path("name").asText(UNKNOWN_VALUE) : UNKNOWN_VALUE;
    }

    private String getLocaleValue(JsonNode valuesArray, String locale) {
        if (valuesArray == null || !valuesArray.isArray()) return NULL_VALUE;
        for (JsonNode valueNode : valuesArray) {
            if (locale.equals(valueNode.path("locale").asText())) {
                return valueNode.path("value").asText(NULL_VALUE);
            }
        }
        return NULL_VALUE;
    }
}