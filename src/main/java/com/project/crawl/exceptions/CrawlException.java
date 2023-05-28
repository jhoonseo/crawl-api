package com.project.crawl.exceptions;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CrawlException extends ResponseStatusException {
    public Type type;

    public CrawlException(Type exceptionType) {
        super(exceptionType.getStatus(), exceptionType.getMessage());
        this.type = exceptionType;
    }

    public CrawlException(Type exceptionType, String message) {
        super(exceptionType.getStatus(), message);
    }

    public Type getType() {
        return this.type;
    }

    @Getter
    public static enum Type {
        BAD_REQUEST("잘못된 요청입니다."),
        FORBIDDEN("Access Denied");

        private String message;
        private HttpStatus status = HttpStatus.BAD_REQUEST;
        Type(HttpStatus status, String message) {
            this.message = message;
            this.status = status;
        }

        Type(String message) {
            this.message = message;
        }
    }
}
