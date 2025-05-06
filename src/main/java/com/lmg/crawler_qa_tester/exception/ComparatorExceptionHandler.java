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
                .errorCode(ex.errorName)
                .errorCode(String.valueOf(ex.httpStatusCode))
                .build();
        return ResponseEntity.status(ex.httpStatusCode).body(errorDetails);
    }
}
