package com.lmg.crawler_qa_tester.service.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmg.crawler_qa_tester.dto.comparator.ApiEntry;
import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import com.lmg.crawler_qa_tester.exception.ComparatorException;
import com.lmg.crawler_qa_tester.util.ExcelUtils;
import com.lmg.crawler_qa_tester.util.JsonNodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompareService {
    private static final Set<String> VALID_LOCALES = Set.of("en", "ar");

    private final ApiService apiService;
    private final ObjectMapper objectMapper;

    public ResponseEntity<String> compare(CompareRequest req) {
        try (Workbook wb = new XSSFWorkbook()) {
            processStandardApis(req, wb);
            processLeftHeaderStripApi(req, wb);

            String fileName = ExcelUtils.generateFileName(req.country(), req.concept());
            ExcelUtils.writeWorkbookToFile(wb, fileName);

            return ResponseEntity.ok("Excel sheet generated successfully. File: " + fileName);
        } catch (ComparatorException e) {
            log.error("Comparator error occurred: {}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("IO error during Excel file operations: {}", e.getMessage(), e);
            throw new ComparatorException("File Operation Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InterruptedException e) {
            log.error("API call interrupted: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new ComparatorException("API Operation Interrupted", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during comparison: {}", e.getMessage(), e);
            throw new ComparatorException("Unexpected Error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void processStandardApis(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        List<ApiEntry> apiEntries = List.of(
                new ApiEntry(RIGHT_HEADER_STRIP_API_NAME, req.concept(), req.country(), RIGHT_HEADER_STRIP_SHEET_NAME),
                new ApiEntry(HEADER_NAV_API_NAME, req.concept(), req.country(), HEADER_NAV_SHEET_NAME),
                new ApiEntry(FOOTER_STRIP_API_NAME, req.concept(), req.country(), FOOTER_STRIP_SHEET_NAME)
        );

        for (ApiEntry entry : apiEntries) {
            for (String lang : LANG_CODES) {
                String url1 = apiService.getApiUrl(req.compareEnvFrom(), entry.country(), entry.concept(), lang, entry.name());
                String url2 = apiService.getApiUrl(req.compareEnvTo(), entry.country(), entry.concept(), lang, entry.name());

                JsonNode json1 = apiService.callApi(url1);
                JsonNode json2 = apiService.callApi(url2);

                String sheetName = entry.sheetName() + "_" + lang;
                Sheet sheet = ExcelUtils.createSheetWithHeader(wb, sheetName, req.compareEnvFrom(), req.compareEnvTo());

                compareJson(json1, json2, "", sheet);
            }
        }
    }

    private void processLeftHeaderStripApi(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        ApiEntry leftHeaderStripEntry = new ApiEntry(
                LEFT_HEADER_STRIP_API_NAME,
                req.concept(),
                req.country(),
                LEFT_HEADER_STRIP_SHEET_NAME
        );

        String url1 = apiService.getApiUrl(req.compareEnvFrom(), leftHeaderStripEntry.country(),
                leftHeaderStripEntry.concept(), null, leftHeaderStripEntry.name());
        String url2 = apiService.getApiUrl(req.compareEnvTo(), leftHeaderStripEntry.country(),
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
            JsonNode child1 = JsonNodeUtils.safeGet(node1, i);
            JsonNode child2 = JsonNodeUtils.safeGet(node2, i);

            JsonNode content1 = JsonNodeUtils.getContent(child1);
            JsonNode content2 = JsonNodeUtils.getContent(child2);

            String name = JsonNodeUtils.getFieldValue(content1, content2, "name");
            String order = JsonNodeUtils.getFieldValue(content1, content2, "displayOrder");
            String url1 = JsonNodeUtils.getFieldValue(content1, "url");
            String url2 = JsonNodeUtils.getFieldValue(content2, "url");

            String currentPath = buildPath(parentPath, name, order);
            ExcelUtils.writeComparisonRow(sheet, currentPath, url1, url2);

            JsonNode children1 = JsonNodeUtils.getChildren(content1);
            JsonNode children2 = JsonNodeUtils.getChildren(content2);

            if (JsonNodeUtils.hasChildren(children1) || JsonNodeUtils.hasChildren(children2)) {
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
                sheets.put(locale, ExcelUtils.getOrCreateSheet(wb, baseName + "_" + locale, req.compareEnvFrom(), req.compareEnvTo()))
        );

        traverseAndCompare(json1, json2, "", sheets);
    }

    private void traverseAndCompare(JsonNode node1, JsonNode node2, String parentPath, Map<String, Sheet> sheets) {
        if (JsonNodeUtils.bothNodesNullOrEmpty(node1, node2)) return;

        Set<String> fieldNames = JsonNodeUtils.collectFieldNames(node1, node2);

        fieldNames.forEach(field -> {
            JsonNode child1 = JsonNodeUtils.safeGet(node1, field);
            JsonNode child2 = JsonNodeUtils.safeGet(node2, field);
            String currentPath = buildPath(parentPath, field);

            if ("messages".equals(field)) {
                compareMessagesArray(child1, child2, sheets);
                return;
            }

            if (JsonNodeUtils.isContainerNode(child1) || JsonNodeUtils.isContainerNode(child2)) {
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
            JsonNode message1 = JsonNodeUtils.safeGet(messages1, i);
            JsonNode message2 = JsonNodeUtils.safeGet(messages2, i);

            String messageName = JsonNodeUtils.getMetaName(message1 != null ? message1 : message2);
            JsonNode description1 = JsonNodeUtils.getDescriptionValues(message1);
            JsonNode description2 = JsonNodeUtils.getDescriptionValues(message2);

            VALID_LOCALES.forEach(locale -> {
                String val1 = JsonNodeUtils.getLocaleValue(description1, locale);
                String val2 = JsonNodeUtils.getLocaleValue(description2, locale);
                ExcelUtils.writeComparisonRow(sheets.get(locale), messageName, val1, val2);
            });
        }
    }

    private String buildPath(String parent, String... parts) {
        String joinedParts = String.join("_", parts);
        return parent.isEmpty() ? joinedParts : parent + "/" + joinedParts;
    }
}