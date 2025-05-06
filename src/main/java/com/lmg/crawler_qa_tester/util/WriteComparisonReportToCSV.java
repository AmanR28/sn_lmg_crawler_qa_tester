package com.lmg.crawler_qa_tester.util;

import com.lmg.crawler_qa_tester.constants.EnvironmentEnum;
import com.lmg.crawler_qa_tester.repository.entity.CrawlDetailEntity;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WriteComparisonReportToCSV {
    public byte[] writeMissingReportToCSV(List<CrawlDetailEntity> crawlDetails, Map<String,CrawlDetailEntity> prodSet, Map<String,CrawlDetailEntity>  preProdSet) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
        // Write the header row
        String[] header = {"Path", EnvironmentEnum.PROD.getValue(), EnvironmentEnum.PRE_PROD.getValue()};
        writer.writeNext(header);
        Set<String> uniquePath = new HashSet<String>();
        for(CrawlDetailEntity detail:crawlDetails)
        {
            String path = detail.getPath();
            if(uniquePath.contains(path))
            {
                continue;
            }
            uniquePath.add(path);
            String prodProcessFlag = prodSet.containsKey(path)?prodSet.get(path).getProcessFlag():"Missing";
            String preProdProcessFlag = preProdSet.containsKey(path)?preProdSet.get(path).getProcessFlag():"Missing";
            writeDetailsToCsv(path,prodProcessFlag,preProdProcessFlag,writer);
        }
    } catch (
    IOException e) {
        throw new IOException("Error while writing to CSV byte array: " + e.getMessage(), e);
    }

        return outputStream.toByteArray();
}

private void writeDetailsToCsv(String path,String prodStatus,String preProdStatus, CSVWriter writer) throws IOException {

        String[] record = {

                path,
                prodStatus,
                preProdStatus
        };
        writer.writeNext(record);

}
}
