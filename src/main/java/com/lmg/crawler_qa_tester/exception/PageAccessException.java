package com.lmg.crawler_qa_tester.exception;

public class PageAccessException extends RuntimeException {

    private final String url;
    private final Integer statusCode;

    public PageAccessException(String message, String url, Integer statusCode) {

        super(message);
        this.url = url;
        this.statusCode = statusCode;
    }

    public PageAccessException(String message, String url) {

        this(message, url, null);
    }

    public String getUrl() {

        return url;
    }

    public Integer getStatusCode() {

        return statusCode;
    }

}
