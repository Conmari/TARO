package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.dto.auth.LoginRequest;
import scari.corp.taro.dto.auth.RegisterRequest;
import scari.corp.taro.service.AuthService;

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
        authService.register(request, req.getSession().getId());

        return ResponseEntity.ok(new ApiResponse("Регистрация успешна"));

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest req) {
        log.debug("Попытка входа: {}", request.username());
        authService.login(request, req.getSession().getId(), req);

        return ResponseEntity.ok(new ApiResponse("Вход успешен"));
    }
}
