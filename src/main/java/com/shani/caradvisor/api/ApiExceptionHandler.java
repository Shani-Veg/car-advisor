package com.shani.caradvisor.api;

import com.shani.caradvisor.workflow.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;

/** Errors as {"error":{"code","message"}}. No stack traces or internals reach the client. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException e) {
        return error(e.status(), e.code(), e.getMessage());
    }

    /** Two requests changed the same case at the same moment; the second one must reload. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<Map<String, Object>> concurrentChange(ObjectOptimisticLockingFailureException e) {
        return error(409, "VERSION_CONFLICT", "המידע התעדכן בינתיים. טענו מחדש ובדקו שוב.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        return error(400, "INVALID_JSON", "גוף הבקשה אינו JSON תקין");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, Object>> tooLarge(MaxUploadSizeExceededException e) {
        return error(400, "FILE_TOO_LARGE", "הקובץ גדול מדי. אפשר להעלות עד 5 מגה.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<Map<String, Object>> noFile(MissingServletRequestPartException e) {
        return error(400, "EMPTY_FILE", "לא התקבל קובץ");
    }

    private static ResponseEntity<Map<String, Object>> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("error", Map.of("code", code, "message", message)));
    }
}
