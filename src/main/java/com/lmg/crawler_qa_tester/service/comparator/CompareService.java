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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import static com.lmg.crawler_qa_tester.util.ComparatorConstants.*;

@Service
@Slf4j
public class CompareService {

    @Autowired
    private ApiService apiService;

    public ResponseEntity<String> compare(CompareRequest req) {
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
                    header.createCell(1).setCellValue(req.firstEnv);
                    header.createCell(2).setCellValue(req.secondEnv);
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
            return new ResponseEntity<>("Excel sheet generated, fileName - " + fileName, HttpStatus.OK);
        } catch (ComparatorException e) {
            log.info("ComparatorException - {}", e.getMessage());
            throw new ComparatorException(e.getErrorName(), e.getErrorMessage(), e.getHttpStatusCode());
        } catch (InterruptedException | IOException e) {
            throw new ComparatorException(e.getCause().toString(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void compareJson(JsonNode node1, JsonNode node2, String parentPath, Sheet sheet, int[] rowNum) {
        int max = Math.max(
                (node1 != null && node1.isArray()) ? node1.size() : 0,
                (node2 != null && node2.isArray()) ? node2.size() : 0
        );

        for (int i = 0; i < max; i++) {
            JsonNode child1 = (node1 != null && i < node1.size()) ? node1.get(i) : null;
            JsonNode child2 = (node2 != null && i < node2.size()) ? node2.get(i) : null;

            JsonNode content1 = (child1 != null) ? child1.path("content") : null;
            JsonNode content2 = (child2 != null) ? child2.path("content") : null;

            String name = content1 != null && content1.has("name") ? safeText(content1.path("name")) :
                    content2 != null && content2.has("name") ? safeText(content2.path("name")) : "null";
            String order = content1 != null && content1.has("displayOrder") ? safeText(content1.path("displayOrder")) :
                    content2 != null && content2.has("displayOrder") ? safeText(content2.path("displayOrder")) : "null";

            String url1 = (content1 != null && content1.has("url")) ? safeText(content1.path("url")) : "null";
            String url2 = (content2 != null && content2.has("url")) ? safeText(content2.path("url")) : "null";

            String currentPath = buildPath(parentPath, name, order);
            boolean match = url1.equals(url2);

            writeRow(sheet, rowNum, currentPath, url1, url2, match);

            JsonNode children1 = (content1 != null) ? content1.path("children") : null;
            JsonNode children2 = (content2 != null) ? content2.path("children") : null;

            if ((children1 != null && children1.isArray() && children1.size() > 0) ||
                    (children2 != null && children2.isArray() && children2.size() > 0)) {
                compareJson(
                        (children1 != null && children1.isArray()) ? children1 : new ObjectMapper().createArrayNode(),
                        (children2 != null && children2.isArray()) ? children2 : new ObjectMapper().createArrayNode(),
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
