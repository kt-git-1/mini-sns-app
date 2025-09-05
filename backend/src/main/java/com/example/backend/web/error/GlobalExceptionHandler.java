package com.example.backend.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String path(HttpServletRequest req){ return req.getRequestURI(); }

    // Bean Validation: @Valid DTO のバリデーション失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req){
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return ErrorResponse.of("invalid_request", msg, path(req), HttpStatus.BAD_REQUEST.value());
    }

    private String formatFieldError(FieldError fe){
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage());
    }

    // @Validated パラメータ（@Min/@Max 等）の失敗
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req){
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return ErrorResponse.of("invalid_request", msg, path(req), HttpStatus.BAD_REQUEST.value());
    }

    // クエリ/Path型不一致、必須パラメータ欠落、JSONパース失敗
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ErrorResponse handleRequestFormat(Exception ex, HttpServletRequest req){
        return ErrorResponse.of("invalid_request", ex.getMessage(), path(req), HttpStatus.BAD_REQUEST.value());
    }

    // 競合（例：username UNIQUE違反）
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req){
        return ErrorResponse.of("conflict", "duplicate or integrity violation", path(req), HttpStatus.CONFLICT.value());
    }

    // 明示的に投げた ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse handleRSE(ResponseStatusException ex, HttpServletRequest req){
        var status = ex.getStatusCode().value();
        var code = status == 401 ? "unauthorized"
                : status == 403 ? "forbidden"
                : status == 404 ? "not_found"
                : "invalid_request";
        var message = ex.getReason() == null ? ex.getMessage() : ex.getReason();
        return ErrorResponse.of(code, message, path(req), status);
    }

    // Spring 6+ の ErrorResponseException（NotImplemented など）
    @ExceptionHandler(ErrorResponseException.class)
    public ErrorResponse handleERE(ErrorResponseException ex, HttpServletRequest req){
        var status = ex.getStatusCode().value();
        return ErrorResponse.of("error", ex.getBody().getDetail(), path(req), status);
    }

    // 最後の砦
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAny(Exception ex, HttpServletRequest req){
        // ここはログ出力のみして、クライアントへは汎用メッセージを返す
        ex.printStackTrace();
        return ErrorResponse.of("internal_error", "unexpected error", path(req), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
