package scari.corp.taro.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.User;
import scari.corp.taro.entity.UserAccount;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.repository.TaroLayoutRepository;
import scari.corp.taro.repository.UserAccountRepository;
import scari.corp.taro.repository.UserRepository;
import scari.corp.taro.service.TaroService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фасад для интеграции чат-платформ и веб-интерфейса.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaroBotFacade {

    private final UserAccountRepository userAccountRepository;
    private final TaroLayoutRepository taroLayoutRepository;
    private final UserRepository userRepository;
    private final TaroService taroService;

    private final Map<String, LinkCodeDetails> linkCodesStorage = new ConcurrentHashMap<>();

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Вспомогательный рекорд для хранения деталей пин-кода в оперативной памяти.
     */
    private record LinkCodeDetails(String provider, String providerId, LocalDateTime expiresAt) {}

    /**
     * Генерирует расклад карт Таро с автоматическим распределением гостевых и веб-сессий.
     */
    public List<CardResponseDto> executeLayoutDynamic(BotProvider provider, String providerId, LayoutType layoutType) {
        User user = userAccountRepository.findUserByAccountFields(provider.name(), providerId).orElse(null);

        String username = (user != null) ? user.getUsername() : null;
        String sessionId = (user == null) ? buildSessionId(provider, providerId) : null;

        log.info("[Фасад] Запрос расклада {} от платформы {}. Статус: {}",
                layoutType, provider, (user != null ? "Авторизован (" + username + ")" : "Гость"));

        return taroService.generateLayout(username, sessionId, layoutType);
    }

    /**
     * Возвращает последние сохраненные расклады для авторизованного профиля или гостя мессенджера.
     */
    public List<TaroHistoryResponseDto> getLatestReadingsDynamic(BotProvider provider, String providerId, int count) {
        User user = userAccountRepository.findUserByAccountFields(provider.name(), providerId).orElse(null);

        String username = (user != null) ? user.getUsername() : null;
        String sessionId = (user == null) ? buildSessionId(provider, providerId) : null;

        Page<TaroHistoryResponseDto> page = taroService.getLastReadings(username, sessionId, 0, count);
        return page.getContent();
    }

    /**
     * Генерирует случайный криптостойкий одноразовый 6-значный код верификации для чата.
     */
    public String generateLinkCodeForChat(BotProvider provider, String providerId) {
        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        linkCodesStorage.put(code, new LinkCodeDetails(provider.name(), providerId, LocalDateTime.now().plusMinutes(5)));

        log.info("[Фасад] Сгенерирован защищенный код привязки для платформы {} (ID: {})", provider, providerId);
        return code;
    }

    /**
     * Подтверждает привязку внешнего аккаунта со стороны веб-сайта и выполняет слияние гостевой истории.
     */
    @Transactional
    public boolean confirmLinkOnWebSite(String webUsername, String inputCode) {
        LinkCodeDetails details = linkCodesStorage.remove(inputCode);

        if (details == null || LocalDateTime.now().isAfter(details.expiresAt())) {
            log.warn("[Фасад] Попытка привязки по неверному, использованному или истекшему коду: {}", inputCode);
            return false;
        }

        User webUser = userRepository.findByUsername(webUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь сайта не найден: " + webUsername));

        String provider = details.provider();
        String providerUserId = details.providerId();

        if (userAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new IllegalStateException("Этот аккаунт " + provider + " уже привязан к другому профилю!");
        }

        UserAccount newAccount = UserAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .user(webUser)
                .build();
        userAccountRepository.save(newAccount);

        String guestSessionId = buildSessionId(BotProvider.valueOf(provider), providerUserId);
        taroLayoutRepository.linkSessionToUser(webUser, guestSessionId);

        log.info("[Фасад] Аккаунт {} ({}) успешно связан с ЛК сайта '{}'. История перенесена.",
                provider, providerUserId, webUsername);
        return true;
    }

    /**
     * Планировщик для очистки просроченных кодов.
     * Автоматически запускается раз в 10 минут и удаляет «мусор» из оперативной памяти.
     */
    @Scheduled(fixedRate = 600000)
    public void cleanExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        int initialSize = linkCodesStorage.size();

        linkCodesStorage.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt()));

        int removedCount = initialSize - linkCodesStorage.size();
        if (removedCount > 0) {
            log.info("[Память] Из кэша успешно удалено просроченных кодов привязки: {}", removedCount);
        }
    }

    /**
     * Централизованный метод генерации ключей сессий.
     */
    private String buildSessionId(BotProvider provider, String providerId) {
        return provider.name().toLowerCase() + "_" + providerId;
    }
}
