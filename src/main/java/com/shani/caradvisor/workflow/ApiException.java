package com.shani.caradvisor.workflow;

/** A failure the client should see, with a stable code and a Hebrew message. */
public class ApiException extends RuntimeException {

    private final int status;
    private final String code;

    public ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String code, String message) {
        return new ApiException(400, code, message);
    }

    public static ApiException notFound(String code, String message) {
        return new ApiException(404, code, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(409, code, message);
    }

    public static ApiException unavailable(String code, String message) {
        return new ApiException(503, code, message);
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
