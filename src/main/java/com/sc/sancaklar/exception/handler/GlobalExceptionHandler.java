package com.sc.sancaklar.exception.handler;

import com.sc.sancaklar.exception.AlreadyExistException;
import com.sc.sancaklar.exception.AnErrorOccurredException;
import com.sc.sancaklar.exception.MailOrPasswordIncorrectException;
import com.sc.sancaklar.exception.NotFoundException;
import com.sc.sancaklar.exception.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage().concat(" ").concat("Kaydı sistemde zaten kayıtlı."), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage().concat(" ").concat("Kaydı bulunamadı."), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AnErrorOccurredException.class)
    public ResponseEntity<ErrorResponse> handleAnErrorOccurredException(AnErrorOccurredException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage().concat(" ").concat("bir hata oluştu."), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MailOrPasswordIncorrectException.class)
    public ResponseEntity<ErrorResponse> handleMailOrPasswordIncorrectException(MailOrPasswordIncorrectException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Mail veya şifre hatalı.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        // Swagger isteklerini işleme
        if (request.getRequestURI().startsWith("/v3/api-docs") || request.getRequestURI().startsWith("/swagger-ui")) {
            return ResponseEntity.ok().build(); // Swagger dokümantasyonunu kendi başına işlemesi için izin ver
        }
        // Diğer hatalar için genel yanıt
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Beklenmeyen bir hata oluştu\", \"status\": 500}".concat("errorDetail:").concat(e.getMessage()));
    }
}
