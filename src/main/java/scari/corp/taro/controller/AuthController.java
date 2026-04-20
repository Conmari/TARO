package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.dto.auth.LoginRequest;
import scari.corp.taro.dto.auth.RegisterRequest;
import scari.corp.taro.service.AuthService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletRequest req) {
        log.debug("Попытка регистрации нового пользователя: {}", request.username());
        authService.register(request, req);

        return ResponseEntity.ok(new ApiResponse("Регистрация успешна"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest req) {
        log.debug("Попытка входа: {}", request.username());
        authService.login(request, req);

        return ResponseEntity.ok(new ApiResponse("Вход успешен"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new ApiResponse("Выход выполнен"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse("Не авторизован"));
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }
}
