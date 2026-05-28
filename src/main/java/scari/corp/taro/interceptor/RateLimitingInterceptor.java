package scari.corp.taro.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import scari.corp.taro.annotation.AuthRateLimit;
import scari.corp.taro.annotation.TaroRateLimit;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.service.RateLimitingService;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * Интерцептор для сквозной проверки лимитов частоты запросов.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final String TARO_LIMIT_MSG = "Вы совершаете запросы слишком часто. Пожалуйста, подождите 3 секунды.";
    private static final String AUTH_LIMIT_MSG = "Слишком много попыток входа с вашего компьютера. Пожалуйста, подождите минуту.";
    private static final String IP_PREFIX = "IP:";

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (handlerMethod.hasMethodAnnotation(TaroRateLimit.class)) {
            String limitKey = resolveTaroLimitKey(request);
            if (!rateLimitingService.tryConsume(limitKey)) {
                sendErrorResponse(response, TARO_LIMIT_MSG);
                return false;
            }
        }

        if (handlerMethod.hasMethodAnnotation(AuthRateLimit.class)) {
            String ipAddress = getClientIp(request);
            if (!rateLimitingService.tryConsumeAuth(IP_PREFIX + ipAddress)) {
                sendErrorResponse(response, AUTH_LIMIT_MSG);
                return false;
            }
        }

        return true;
    }

    /**
     * Безопасно вычисляет ключ лимита для карт Таро без принудительного создания пустых сессий.
     */
    private String resolveTaroLimitKey(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName() != null) {
            return principal.getName();
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getId();
        }

        return getClientIp(request);
    }

    /**
     * Прерывает обработку запроса и возвращает клиенту валидный JSON-ответ.
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResponse apiResponse = new ApiResponse(message);
        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }

    /**
     * Извлекает реальный IP-адрес клиента с учетом проксирования через Nginx.
     * <p>
     * Анализирует HTTP-заголовок 'X-Forwarded-For'. Если запрос прошел цепочку прокси,
     * извлекает самый первый (оригинальный) IP-адрес. Если заголовок отсутствует,
     * возвращает стандартный удаленный адрес хоста.
     *
     * @param request текущий HTTP-запрос
     * @return строковое представление IP-адреса клиента
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
