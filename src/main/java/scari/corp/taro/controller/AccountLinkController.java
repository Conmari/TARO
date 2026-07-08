package scari.corp.taro.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scari.corp.taro.dto.integration.LinkRequestDto;
import scari.corp.taro.facade.TaroBotFacade;

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

    private final TaroBotFacade taroFacade;

    /**
     * Подключает аккаунт Telegram к профилю на сайте по коду верификации из бота.
     */
    @PostMapping("/telegram")
    public ResponseEntity<?> linkTelegramAccount(
            @Valid @RequestBody LinkRequestDto request,
            Principal principal) {

        String webUsername = principal.getName();
        log.info("[API Сайта] Запрос привязки Telegram от пользователя '{}'", webUsername);

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
}
