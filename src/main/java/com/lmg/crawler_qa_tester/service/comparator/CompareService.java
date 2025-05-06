package com.lmg.crawler_qa_tester.service.comparator;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmg.crawler_qa_tester.dto.comparator.ApiEntry;
import com.lmg.crawler_qa_tester.dto.comparator.CompareRequest;
import com.lmg.crawler_qa_tester.exception.ComparatorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.util.List;
import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Slf4j
public class CompareService {

    @Autowired
    private ApiService apiService;

    public void compare(CompareRequest req) throws Exception {
        try {
            List<ApiEntry> apiEntries = List.of(
                    new ApiEntry(HEADER_STRIP_API_NAME, req.concept, req.country),
                    new ApiEntry(HEADER_NAV_API_NAME, req.concept, req.country),
                    new ApiEntry(FOOTER_STRIP_API_NAME, req.concept, req.country)
            );

            Workbook wb = new XSSFWorkbook();

            for (ApiEntry entry : apiEntries) {
                for (String lang : LANG_CODES) {
                    String url1 = apiService.getApiUrl(req.firstEnv, entry.country(), entry.concept(), lang, entry.name());
                    String url2 = apiService.getApiUrl(req.secondEnv, entry.country(), entry.concept(), lang, entry.name());

                    JsonNode json1 = apiService.callApi(url1);
                    JsonNode json2 = apiService.callApi(url2);

                    String sheetName = entry.name() + "_" + lang;
                    Sheet sheet = wb.createSheet(sheetName);

                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue(SHEET_COLUMN1);
                    header.createCell(1).setCellValue(req.firstEnv + SHEET_COLUMN2);
                    header.createCell(2).setCellValue(req.secondEnv + SHEET_COLUMN3);
                    header.createCell(3).setCellValue(SHEET_COLUMN4);

                    int[] rowNum = {1};

                    compareJson(json1, json2, "", sheet, rowNum);
                }
            }

            String timestamp = java.time.LocalDateTime.now().toString().replace(":", "_");
            String fileName = String.format("%s_%s_%s.xlsx", req.country, req.concept, timestamp);
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                wb.write(fos);
            }
            wb.close();

            log.info("Comparison written to {}", fileName);
        } catch (Exception e) {
            log.info("Exception - {}", e.getMessage());
            throw new ComparatorException("comparator exception", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void compareJson(JsonNode node1, JsonNode node2, String parentPath, Sheet sheet, int[] rowNum) {
        if (node1 == null || !node1.isArray() || node2 == null || !node2.isArray()) return;

        int max = Math.max(node1.size(), node2.size());

        for (int i = 0; i < max; i++) {
            JsonNode child1 = i < node1.size() ? node1.get(i) : null;
            JsonNode child2 = i < node2.size() ? node2.get(i) : null;

            JsonNode content1 = child1 != null ? child1.path("content") : null;
            JsonNode content2 = child2 != null ? child2.path("content") : null;

            String name = content1 != null ? safeText(content1.path("name")) : "null";
            String order = content1 != null ? safeText(content1.path("displayOrder")) : "null";
            String url1 = content1 != null ? safeText(content1.path("url")) : "null";
            String url2 = content2 != null ? safeText(content2.path("url")) : "null";

            String currentPath = buildPath(parentPath, name, order);
            boolean match = url1.equals(url2);

            writeRow(sheet, rowNum, currentPath, url1, url2, match);

            JsonNode children1 = content1 != null ? content1.path("children") : null;
            JsonNode children2 = content2 != null ? content2.path("children") : null;

            if ((children1 != null && children1.isArray()) || (children2 != null && children2.isArray())) {
                compareJson(
                        children1 != null ? children1 : new ObjectMapper().createArrayNode(),
                        children2 != null ? children2 : new ObjectMapper().createArrayNode(),
                        currentPath,
                        sheet,
                        rowNum
                );
            }
        }
    }
    private String safeText(JsonNode node) {
        return (node == null || node.isNull()) ? "null" : node.asText();
    }

    private String buildPath(String parent, String name, String order) {
        String part = name + "_" + order;
        return parent.isEmpty() ? part : parent + "/" + part;
    }

    private void writeRow(Sheet sheet, int[] rowNum, String path, String val1, String val2, boolean match) {
        Row row = sheet.createRow(rowNum[0]++);
        row.createCell(0).setCellValue(path);
        row.createCell(1).setCellValue(val1);
        row.createCell(2).setCellValue(val2);
        row.createCell(3).setCellValue(match ? "YES" : "NO");
    }

}
