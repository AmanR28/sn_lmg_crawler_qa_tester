package com.lmg.crawler_qa_tester.exception;

import com.lmg.crawler_qa_tester.dto.comparator.ErrorDetails;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
@Log4j2
@ControllerAdvice
public class ComparatorExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { ComparatorException.class })
    public ResponseEntity<Object> handleComparatorException(ComparatorException ex) {
        log.info(ex.getMessage(), ex);
        var errorDetails = ErrorDetails.builder()
                .errorMessage(ex.errorMessage)
                .errorName(ex.errorName)
                .errorCode(String.valueOf(ex.httpStatusCode))
                .build();
        return ResponseEntity.status(ex.httpStatusCode).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.info(ex.getMessage(), ex);
        var errorDetails = ErrorDetails.builder()
                .errorMessage(ex.getMessage())
                .errorName("Exception")
                .errorCode("999")
                .build();
        return ResponseEntity.status(500).body(errorDetails);
    }
}
