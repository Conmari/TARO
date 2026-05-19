package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletRequest req) {
        log.debug("Попытка регистрации нового пользователя: {}", request.username());
        String sessionId = req.getSession().getId();

        authService.register(request.username(), request.password(), sessionId);

        authenticateAndStoreSession(request.username(), request.password(), req);

        return ResponseEntity.ok(new ApiResponse("Регистрация успешна"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest req) {
        log.debug("Попытка входа: {}", request.username());
        String sessionId = req.getSession().getId();

        authenticateAndStoreSession(request.username(), request.password(), req);

        authService.processPostLoginHistory(request.username(), sessionId);

        return ResponseEntity.ok(new ApiResponse("Вход успешен"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "unknown";

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        log.info("Пользователь вышел из системы: {}", username);
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

    // Вспомогательный метод для работы с сессией
    private void authenticateAndStoreSession(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}
