package com.lmg.crawler_qa_tester.util;

import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class CsvUtil {
  public static byte[] getCsvData(List<String> headers, List<String[]> data) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {
      writer.writeNext(headers.toArray(new String[0]));
      for (String[] row : data) {
        writer.writeNext(row);
      }
      writer.flush();
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Error generating CSV: " + e.getMessage(), e);
    }
  }
}
