package br.gov.es.openpmo.sigef_core.exception;

public class GlobalException extends RuntimeException {
    public GlobalException(String message, Throwable cause) {
        super(message, cause);
    }
}
