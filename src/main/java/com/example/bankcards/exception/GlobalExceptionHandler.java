package com.example.bankcards.exception;

import com.example.bankcards.dto.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.xml.bind.ValidationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardAccessException.class)
    public ResponseEntity<ErrorResponse> handleCardAccessException(final CardAccessException ex) {
        log.warn("неверный доступ к карте", ex);
        ErrorResponse errorResponse = new ErrorResponse("нет доступа к данной карте", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(CardNumberCreationException.class)
    public ResponseEntity<ErrorResponse> handleCardNumberCreationException(final CardNumberCreationException ex) {
        log.warn("не удалось создать уникальный номер карты", ex);
        ErrorResponse errorResponse = new ErrorResponse("не удалось создать уникальный номер карты", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(final JwtAuthenticationException ex) {
        log.warn("ошибка при проверке jwt пользователя", ex);
        ErrorResponse errorResponse = new ErrorResponse("ошибка при проверке jwt пользователя", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
        log.warn("not found", ex);
        ErrorResponse errorResponse = new ErrorResponse("not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<ErrorResponse> handleTransferException(final TransferException ex) {
        log.warn("transfer exception", ex);
        ErrorResponse errorResponse = new ErrorResponse("transfer error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExistsException(final UsernameAlreadyExistsException ex) {
        log.warn("имя пользователя уже занято", ex);
        ErrorResponse errorResponse = new ErrorResponse("имя пользователя уже занято", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException ex) {
        log.warn("error", ex);
        ErrorResponse errorResponse = new ErrorResponse("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(final ValidationException ex) {
        log.warn("ошибка валидации", ex);
        ErrorResponse errorResponse = new ErrorResponse("ошибка валидации", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex){
        log.warn("ошибка валидации", ex);
        ErrorResponse errorResponse = new ErrorResponse("ошибка валидации", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final UsernameAlreadyExistsException ex) {
        log.warn("error", ex);
        ErrorResponse errorResponse = new ErrorResponse("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
