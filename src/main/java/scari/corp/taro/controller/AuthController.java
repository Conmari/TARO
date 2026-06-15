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
import scari.corp.taro.annotation.AuthRateLimit;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.dto.auth.LoginRequest;
import scari.corp.taro.dto.auth.RegisterRequest;
import scari.corp.taro.service.AuthService;
import scari.corp.taro.service.RateLimitingService;

import java.security.Principal;
import java.util.Map;

/**
 * Контроллер для управления аутентификацией, регистрацией пользователей и сессиями.
 * <p>
 * Обеспечивает безопасность процессов авторизации, включая встроенную защиту
 * от брутфорса на основе IP-адресов и имён пользователей.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final RateLimitingService rateLimitingService;

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Создает учетную запись в базе данных, привязывает к ней историю раскладов
     * текущей анонимной сессии и автоматически авторизует пользователя.
     *
     * @param request DTO с регистрационными данными
     * @param req     HTTP-запрос для получения идентификатора текущей сессии
     * @return {@link ResponseEntity} со статусом 200 при успешной регистрации
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
                                      HttpServletRequest req) {
        log.debug("Попытка регистрации нового пользователя: {}", request.username());
        String sessionId = req.getSession().getId();

        authService.register(request.username(), request.password(), sessionId);

        authenticateAndStoreSession(request.username(), request.password(), req);

        return ResponseEntity.ok(new ApiResponse("Регистрация успешна"));
    }

    /**
     * Аутентифицирует пользователя в системе с проверкой лимитов на брутфорс.
     * <p>
     * Метод ограничивает количество попыток входа {@link RateLimitingService#tryConsumeAuth(String)} отдельно
     * по IP-адресу клиента и по запрашиваемому имени пользователя. В случае успеха
     * переносит анонимную историю гаданий на аккаунт.
     *
     * @param request DTO с логином и паролем для входа
     * @param req     HTTP-запрос для определения IP-адреса и управления сессией
     * @return {@link ResponseEntity} со статусом 200 при успехе, либо 429 при превышении лимитов входа
     */
    @AuthRateLimit
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest req) {
        log.debug("Попытка входа: {}", request.username());
        String username = request.username();
        String sessionId = req.getSession().getId();

        // Защита по имени пользователя (максимум 5 попыток в минуту для одного аккаунта)
        if (!rateLimitingService.tryConsumeAuth("USER:" + username)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse("Слишком много попыток входа для этого аккаунта. Пожалуйста, подождите минуту."));
        }

        authenticateAndStoreSession(username, request.password(), req);
        authService.processPostLoginHistory(username, sessionId);

        return ResponseEntity.ok(new ApiResponse("Вход успешен"));
    }

    /**
     * Завершает текущую сессию пользователя.
     * <p>
     * Уничтожает HTTP-сессию сервлета и полностью очищает контекст безопасности Spring Security.
     *
     * @param request HTTP-запрос для управления текущей веб-сессией
     * @return {@link ResponseEntity} со статусом 200 и подтверждением выхода
     */
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

    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     * <p>
     * Используется фронтендом для проверки состояния авторизации сессии.
     *
     * @param principal объект текущего пользователя в контексте безопасности
     * @return {@link ResponseEntity} с именем пользователя, либо 401, если сессия анонимна
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Не авторизован"));
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    /**
     * Вспомогательный метод для программной аутентификации и сохранения контекста в HttpSession.
     */
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
