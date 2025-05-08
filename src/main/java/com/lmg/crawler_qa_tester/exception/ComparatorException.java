package com.lmg.crawler_qa_tester.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class ComparatorException extends RuntimeException {

    String errorName;
    String errorMessage;
    HttpStatus httpStatusCode;
    public ComparatorException(String name, String message, HttpStatus httpStatusCode) {
        super(message);
        this.errorMessage = message;
        this.errorName = name;
        this.httpStatusCode = httpStatusCode;
    }
}
