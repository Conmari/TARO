package scari.corp.taro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import scari.corp.taro.dto.auth.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Неверный логин или пароль"));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExists(UserAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse(e.getMessage()));
    }

    /**
     * Перехватывает ошибки конфликта бизнес-логики (например, дубликаты привязок аккаунтов).
     * Возвращает статус 409 Conflict.
     */
    @ExceptionHandler(AccountIntegrationException.class)
    public ResponseEntity<ApiResponse> handleIllegalState(AccountIntegrationException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse(e.getMessage()));
    }
}
