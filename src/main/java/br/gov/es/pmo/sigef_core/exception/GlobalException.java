package br.gov.es.pmo.sigef_core.exception;

public class GlobalException extends RuntimeException {
    public GlobalException(String message, Throwable cause) {
        super(message, cause);
    }
}
