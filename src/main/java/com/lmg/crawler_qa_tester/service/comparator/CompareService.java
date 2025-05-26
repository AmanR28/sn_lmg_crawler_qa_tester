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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.json.Json;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            if (isProdEnvironment(req.compareEnvFrom()) || isProdEnvironment(req.compareEnvTo())) {
                processProdComparison(req, wb);
            } else {
                processStandardApis(req, wb);
                processLeftHeaderStripApi(req, wb);
            }

            String fileName = ExcelUtils.generateFileName(req);
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

    private boolean isProdEnvironment(String env) {
        return "prod".equalsIgnoreCase(env);
    }

    private void processProdComparison(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        // Determine which env is prod and non-prod
        String prodEnv = isProdEnvironment(req.compareEnvFrom()) ? req.compareEnvFrom() : req.compareEnvTo();
        String nonProdEnv = isProdEnvironment(req.compareEnvFrom()) ? req.compareEnvTo() : req.compareEnvFrom();

        // First create sheets with non-prod data
        CompareRequest request = new CompareRequest(nonProdEnv, prodEnv, req.country(), req.concept());
        processProdAndNonProdApis(request, wb);
        processLeftHeaderStripForProdAndNonProdApi(request, wb);

        // Now process prod data which comes in different structure
        for (String lang : VALID_LOCALES) {
            // Get prod data which contains all information in one call
            String prodUrl = String.format(Objects.requireNonNull(apiService.getProdApiUrl(prodEnv, req.country(), req.concept())),lang);
            JsonNode prodJson = apiService.callApi(prodUrl);

            if (prodJson != null && prodJson.has("slots")) {
                // Get existing sheets
                Sheet footerSheet = wb.getSheet(FOOTER_STRIP_SHEET_NAME + "_" + lang);
                Sheet headerSheet = wb.getSheet(HEADER_NAV_SHEET_NAME + "_" + lang);
                Sheet headerStripSheet = wb.getSheet(RIGHT_HEADER_STRIP_SHEET_NAME + "_" + lang);

                // Process each slot from prod response
                for (JsonNode slot : prodJson.get("slots")) {
                    String slotId = slot.get("slotId").asText();
                    switch (slotId) {
                        case "FooterSlot":
                            updateSheetWithProdData(slot, footerSheet, prodEnv);
                            break;
                        case "NavigationBarV2Slot":
                            updateSheetWithProdData(slot, headerSheet, prodEnv);
                            break;
                        case "HeaderMessagesSlot":
                            updateSheetWithProdData(slot, headerStripSheet, prodEnv);
                            break;
                        case "StoreLocatorSlot":
                            updateSheetWithProdData(slot, headerStripSheet, prodEnv);
                            break;
                        case "DownloadOurAppsSlot":
                            updateSheetWithProdData(slot, headerStripSheet, prodEnv);
                            break;
                    }
                }
            }
        }

        // Handle left header strip for prod
//        String prodLeftHeaderUrl = apiService.getApiUrl(prodEnv, req.country(), req.concept(), null, LEFT_HEADER_STRIP_API_NAME);
//        JsonNode prodLeftHeaderJson = apiService.callApi(prodLeftHeaderUrl);
//
//        if (prodLeftHeaderJson != null) {
//            for (String locale : VALID_LOCALES) {
//                Sheet leftHeaderSheet = wb.getSheet(LEFT_HEADER_STRIP_SHEET_NAME + "_" + locale);
//                if (leftHeaderSheet != null) {
//                    updateSheetWithProdLeftHeaderData(prodLeftHeaderJson, leftHeaderSheet, prodEnv);
//                }
//            }
//        }
    }

    private void updateSheetWithProdData(JsonNode prodData, Sheet sheet, String prodEnv) {
        if (sheet == null || !prodData.has("components")) return;

        JsonNode components = prodData.get("components");
        for (JsonNode component : components) {
            String path = null;
            String value = null;

            // Extract path and value based on component type
            if (component.has("footerCategoryNavMiddleComponents")) {
                processProdFooterCategories(component.get("footerCategoryNavMiddleComponents"), sheet, prodEnv);
            }
            if (component.has("footerCategoryNavTopComponents")) {
                processProdFooterCategories(component.get("footerCategoryNavTopComponents"), sheet, prodEnv);
            } else if (component.has("homeMainNavNodes")) {
                processProdMainNavNodes(component.get("homeMainNavNodes"), sheet, prodEnv);
            } else if (component.has("messages")) {
                processProdMessages(component.get("messages"), sheet, prodEnv);
            } else if (component.has("linkName") && component.has("url")) {
                path = component.get("linkName").asText();
                value = component.get("url").asText();
            }

            // Update or create row in sheet
            if (path != null && value != null) {
                updateOrCreateRow(sheet, path, value, prodEnv);
            }
        }
    }

    private void processProdFooterCategories(JsonNode categories, Sheet sheet, String prodEnv) {
        for (JsonNode category : categories) {
            String title = category.get("title").asText();
            JsonNode links = category.get("links");
            
            for (JsonNode link : links) {
                String linkName = link.get("linkName").asText();
                String url = link.get("url").asText();
                String path = title + "/" + linkName;
                
                updateOrCreateRow(sheet, path, url, prodEnv);
            }
        }
    }

    private void processProdMainNavNodes(JsonNode mainNavNodes, Sheet sheet, String prodEnv) {
        if (mainNavNodes == null || !mainNavNodes.has("navNodes")) return;

        for (JsonNode navNode : mainNavNodes.get("navNodes")) {
            if (navNode.has("link")) {
                JsonNode link = navNode.get("link");
                String linkName = link.get("linkName").asText();
                String url = link.get("url").asText();
                updateOrCreateRow(sheet, linkName, url, prodEnv);
            }

            if (navNode.has("childNavigationNodes")) {

           // exploring child navigation
                for (JsonNode childNode : navNode.get("childNavigationNodes")) {
                    String parentName = navNode.has("link") ?
                            navNode.get("link").get("linkName").asText() : "";
                    if (childNode.has("links")) {
                        boolean fetchedFirst = false;
                        for (JsonNode link : childNode.get("links") ) {
                            String linkName = link.get("linkName").asText();
                            String url = link.get("url").asText();
                            String path = parentName + "/" + linkName;
                            if(!fetchedFirst)
                            {
                                parentName = path;
                                fetchedFirst = true;
                            }
                            updateOrCreateRow(sheet, path, url, prodEnv);
                        }
                    }
                }
            }
            if(navNode.has("staticNavigationNodesLinks"))
            {
                String parentName = navNode.has("link") ?
                        navNode.get("link").get("linkName").asText() : "";
                for(JsonNode staticNode : navNode.get("staticNavigationNodesLinks"))
                {

                    for (JsonNode link : staticNode.get("links") ) {
                        String linkName = link.get("linkName").asText();
                        String url = link.get("url").asText();
                        String path = parentName + "/" + linkName;
                        updateOrCreateRow(sheet, path, url, prodEnv);
                    }
                }
            }
        }
    }

    private void processProdMessages(JsonNode messages, Sheet sheet, String prodEnv) {
        for (JsonNode message : messages) {
            if (message.has("title") && message.has("description")) {
                String title = message.get("title").asText();
                String description = message.get("description").asText();
                updateOrCreateRow(sheet, title, description, prodEnv);
            }
        }
    }

    private void updateOrCreateRow(Sheet sheet, String path, String value, String prodEnv) {
        int rowNum = findRow(sheet, path);
        value = value.replace("\u00A0", " ").trim();
        if (rowNum == -1) {
            // Create new row with null for non-prod and value for prod
            rowNum = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(path);
            row.createCell(1).setCellValue("null"); // non-prod column empty
            row.createCell(2).setCellValue(value); // prod value
            row.createCell(3).setCellValue("NO");
        } else {
            // Update existing row's prod column
            Row row = sheet.getRow(rowNum);
            row.getCell(2).setCellValue(value);
            String val1 = row.getCell(2).toString();
            String val2 = row.getCell(1).toString();
            val1 = val1.replaceAll("\\s+", " ").trim();
            val2 = val2.replaceAll("\\s+", " ").trim();
            if(val1.equals(val2))
            {
                row.createCell(3).setCellValue("YES");
            }
        }
    }

    private void updateSheetWithProdLeftHeaderData(JsonNode prodData, Sheet sheet, String prodEnv) {
        if (prodData == null || sheet == null) return;

        if (prodData.has("messages")) {
            JsonNode messages = prodData.get("messages");
            for (JsonNode message : messages) {
                if (message.has("title") && message.has("description")) {
                    String title = message.get("title").asText();
                    String description = message.get("description").asText();
                    updateOrCreateRow(sheet, title, description, prodEnv);
                }
            }
        }
    }

    private int findRow(Sheet sheet, String path) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getCell(0) != null && 
                row.getCell(0).getStringCellValue().equalsIgnoreCase(path)) {
                return i;
            }
        }
        return -1;
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

                compareJson(json1, json2, "", sheet, false);
            }
        }
    }

    // process only non prod api and make an entry in xlxs sheet
    private void processProdAndNonProdApis(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        List<ApiEntry> apiEntries = List.of(
                new ApiEntry(RIGHT_HEADER_STRIP_API_NAME, req.concept(), req.country(), RIGHT_HEADER_STRIP_SHEET_NAME),
                new ApiEntry(HEADER_NAV_API_NAME, req.concept(), req.country(), HEADER_NAV_SHEET_NAME),
                new ApiEntry(FOOTER_STRIP_API_NAME, req.concept(), req.country(), FOOTER_STRIP_SHEET_NAME)
        );
        for (String lang : LANG_CODES) {
        for (ApiEntry entry : apiEntries) {

                String url1 = apiService.getApiUrl(req.compareEnvFrom(), entry.country(), entry.concept(), lang, entry.name());

                JsonNode json1 = apiService.callApi(url1);

                String sheetName = entry.sheetName() + "_" + lang;
                Sheet sheet = ExcelUtils.createSheetWithHeader(wb, sheetName, req.compareEnvFrom(), req.compareEnvTo());
            compareJson(json1,null,"", sheet, true);
            }
        }
    }
    private void processLeftHeaderStripForProdAndNonProdApi(CompareRequest req, Workbook wb) throws IOException, InterruptedException {
        ApiEntry leftHeaderStripEntry = new ApiEntry(
                LEFT_HEADER_STRIP_API_NAME,
                req.concept(),
                req.country(),
                LEFT_HEADER_STRIP_SHEET_NAME
        );

        String url1 = apiService.getApiUrl(req.compareEnvFrom(), leftHeaderStripEntry.country(),
                leftHeaderStripEntry.concept(), null, leftHeaderStripEntry.name());
//        String url2 = apiService.getApiUrl(req.compareEnvTo(), leftHeaderStripEntry.country(),
//                leftHeaderStripEntry.concept(), null, leftHeaderStripEntry.name());

        JsonNode json1 = apiService.callApi(url1);
//        JsonNode json2 = apiService.callApi(url2);

        compareJsonLocalized(json1, null, wb, leftHeaderStripEntry.sheetName(), req);
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

    private void compareJson(JsonNode node1, JsonNode node2, String parentPath, Sheet sheet, boolean isProdComparison) {
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
            String currentPath= isProdComparison?
                    buildPath(parentPath, name):
                    buildPath(parentPath, name,order);
            ExcelUtils.writeComparisonRow(sheet, currentPath, url1, url2);
            JsonNode children1 = JsonNodeUtils.getChildren(content1);
            JsonNode children2 = JsonNodeUtils.getChildren(content2);

            if (JsonNodeUtils.hasChildren(children1) || JsonNodeUtils.hasChildren(children2)) {
                compareJson(
                        children1 != null ? children1 : objectMapper.createArrayNode(),
                        children2 != null ? children2 : objectMapper.createArrayNode(),
                        currentPath,
                        sheet,
                        isProdComparison
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

           // String messageName = JsonNodeUtils.getMetaName(message1 != null ? message1 : message2);
            JsonNode description1 = JsonNodeUtils.getDescriptionValues(message1);
            JsonNode description2 = JsonNodeUtils.getDescriptionValues(message2);

            VALID_LOCALES.forEach(locale -> {
                String messageName = JsonNodeUtils.getTitleName(message1 != null ? message1 : message2,locale);
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