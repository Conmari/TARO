package scari.corp.taro.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import scari.corp.taro.dto.auth.ApiResponse;
import scari.corp.taro.dto.integration.LinkRequestDto;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.facade.TaroBotFacade;
import scari.corp.taro.repository.UserAccountRepository;
import scari.corp.taro.repository.UserRepository;

import java.security.Principal;
import java.util.Map;

/**
 * REST-контроллер для интеграции и связывания внешних аккаунтов пользователя.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user/connections")
@RequiredArgsConstructor
public class AccountLinkController {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final TaroBotFacade taroFacade;

    /**
     * Возвращает статус привязки Telegram для текущего авторизованного пользователя.
     */
    @GetMapping("/telegram/status")
    public ResponseEntity<Map<String, Boolean>> getTelegramStatus(Principal principal) {
        String webUsername = principal.getName();

        User webUser = userRepository.findByUsername(webUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь сайта не найден: " + webUsername));

        boolean isLinked = userAccountRepository.existsByUserAndProvider(webUser, BotProvider.TELEGRAM.name());

        return ResponseEntity.ok(Map.of("isLinked", isLinked));
    }

    /**
     * Подключает аккаунт Telegram к профилю на сайте по коду верификации из бота.
     */
    @PostMapping("/telegram")
    public ResponseEntity<?> linkTelegramAccount(
            @Valid @RequestBody LinkRequestDto request,
            Principal principal) {

        String webUsername = principal.getName();

        boolean isLinked = taroFacade.confirmLinkOnWebSite(webUsername, request.code());

        if (isLinked) {
            return ResponseEntity.ok(Map.of(
                    "message", "Telegram аккаунт успешно привязан! Вся история гаданий синхронизирована."
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Неверный или истекший код привязки. Сгенерируйте новый код в боте."
        ));
    }

    /**
     * Эндпоинт для удаления привязки Telegram-аккаунта от профиля пользователя.
     */
    @DeleteMapping("/telegram")
    public ResponseEntity<ApiResponse> unlinkTelegramAccount(Principal principal) {
        String webUsername = principal.getName();

        taroFacade.unlinkAccountOnWebSite(webUsername, BotProvider.TELEGRAM);

        return ResponseEntity.ok(new ApiResponse("Привязка к Telegram успешно удалена."));
    }
}
