package scari.corp.taro.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.service.RateLimitingService;
import scari.corp.taro.service.TaroService;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/taro")
@RequiredArgsConstructor
public class TaroController {

    private final TaroService taroService;
    private final RateLimitingService rateLimitingService;

    /**
     * Возвращает случайную карту Таро с проверкой лимита частоты запросов.
     * <p>
     * Метод ограничивает частоту запросов для одного пользователя или гостевой сессии.
     * Если лимит превышен, возвращается ошибка со статусом 429.
     *
     * @param principal объект авторизованного пользователя (может быть null для гостей)
     * @param req       HTTP-запрос для извлечения идентификатора сессии гостя
     * @return {@link ResponseEntity} со статусом 200 и объектом {@link CardResponseDto} при успехе,
     *         либо со статусом 429 и объектом {@link ApiResponse} при превышении лимита
     */
    @GetMapping("/random")
    public ResponseEntity<?> randomCard(Principal principal, HttpServletRequest req) {
        String username = (principal != null) ? principal.getName() : null;
        String sessionId = req.getSession().getId();
        String limitKey = (username != null) ? username : sessionId;

        if (!rateLimitingService.tryConsume(limitKey)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ApiResponse("Вы совершаете запросы слишком часто. Пожалуйста, подождите 3 секунды."));
        }

        CardResponseDto card = taroService.getRandomCard(username, sessionId);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/history")
    public Page<TaroHistoryResponseDto> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            HttpServletRequest req) {

        String username = (principal != null) ? principal.getName() : null;
        String sessionId = req.getSession().getId();
        return taroService.getLastReadings(username, sessionId, page, size);
    }
}
