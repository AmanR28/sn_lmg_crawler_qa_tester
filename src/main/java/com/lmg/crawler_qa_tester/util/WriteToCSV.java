package com.lmg.crawler_qa_tester.util;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class WriteToCSV {

    // This method writes CSV content to a ByteArrayOutputStream and returns the byte array
    public byte[] writeToCSV(
            List<CrawlDetailEntity> prodListNotFound, List<CrawlDetailEntity> preProdListNotFound,
            List<CrawlDetailEntity> prodListSuccess, List<CrawlDetailEntity> preProdListSuccess,
            List<CrawlDetailEntity> prodListInProgress, List<CrawlDetailEntity> preProdListInProgress,
            List<CrawlDetailEntity> prodListFatal, List<CrawlDetailEntity> preProdListFatal,
            List<CrawlDetailEntity> prodListNotProceed, List<CrawlDetailEntity> preProdListNotProceed) throws IOException {

        // Create a ByteArrayOutputStream to hold the CSV content
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
            // Write the header row
            String[] header = {"Path", "Env", "Status"};
            writer.writeNext(header);

            // Write details to CSV for each list
            writeDetailsToCsv(prodListNotFound, writer, EnvironmentEnum.PROD);
            writeDetailsToCsv(preProdListNotFound, writer, EnvironmentEnum.PRE_PROD);
            writeDetailsToCsv(prodListSuccess, writer, EnvironmentEnum.PROD);
            writeDetailsToCsv(preProdListSuccess, writer, EnvironmentEnum.PRE_PROD);
            writeDetailsToCsv(prodListInProgress, writer, EnvironmentEnum.PROD);
            writeDetailsToCsv(preProdListInProgress, writer, EnvironmentEnum.PRE_PROD);
            writeDetailsToCsv(prodListFatal, writer, EnvironmentEnum.PROD);
            writeDetailsToCsv(preProdListFatal, writer, EnvironmentEnum.PRE_PROD);
            writeDetailsToCsv(prodListNotProceed, writer, EnvironmentEnum.PROD);
            writeDetailsToCsv(preProdListNotProceed, writer, EnvironmentEnum.PRE_PROD);
//            writeDetailsToCsv(prodNotInPreProd, writer, EnvironmentEnum.PROD);
//            writeDetailsToCsv(preProdNotInProd, writer, EnvironmentEnum.PRE_PROD);

        } catch (IOException e) {
            throw new IOException("Error while writing to CSV byte array: " + e.getMessage(), e);
        }

        return outputStream.toByteArray();
    }

    // Helper method to write list data to CSV
    private static void writeDetailsToCsv(List<CrawlDetailEntity> list, CSVWriter writer, EnvironmentEnum environment) throws IOException {
        for (CrawlDetailEntity detail : list) {
            String[] record = {

                    String.valueOf(detail.getPath()),
                    String.valueOf(detail.getEnv()),
                    detail.getProcessFlag()
            };
            writer.writeNext(record);
        }
    }

}
