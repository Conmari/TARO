package scari.corp.taro.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scari.corp.taro.enums.BotProvider;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Потокобезопасный сервис для управления жизненным циклом
 * временных кодов верификации чат-платформ.
 */
@Slf4j
@Service
public class LinkCodeService {

    /**
     * Внутренний рекорд для хранения деталей пин-кода внутри кэша.
     */
    private record LinkCodeDetails(BotProvider provider, String providerId) {}

    /**
     * Публичный DTO для передачи результата в TaroBotFacade.
     */
    public record LinkCodeResult(BotProvider provider, String providerId) {}

    /**
     * Обратный индекс для моментальной защиты от дубликатов за O(1) (содержит providerId -> code).
     */
    private final Map<String, String> userToCodeIndex = new ConcurrentHashMap<>();

    /**
     * Кэш с автоматическим TTL в 5 минут.
     */
    private final Cache<String, LinkCodeDetails> codeCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .removalListener((String key, LinkCodeDetails details, RemovalCause cause) -> {
                if (details != null) {
                    userToCodeIndex.remove(details.providerId(), key);
                    log.debug("[Caffeine] Код {} удален из кэша (Причина: {}). Индекс зачищен.", key, cause);
                }
            })
            .build();

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Генерирует случайный криптостойкий одноразовый 6-значный код верификации чата.
     * Выполняет гарантированную инвалидацию старых кодов пользователя за константное время O(1).
     */
    public String generateCode(BotProvider provider, String providerId) {
        String newCode = String.valueOf(100000 + secureRandom.nextInt(900000));
        LinkCodeDetails details = new LinkCodeDetails(provider, providerId);

        userToCodeIndex.compute(providerId, (_, oldCode) -> {
            if (oldCode != null) {
                codeCache.invalidate(oldCode);
            }
            codeCache.put(newCode, details);

            return newCode;
        });

        log.debug("[LinkCodeService] Сгенерирован код привязки для платформы {} (ID: {})", provider, providerId);
        return newCode;
    }

    /**
     * Просто проверяет код и возвращает данные.
     */
    public Optional<LinkCodeResult> verifyCodeWithoutConsuming(String inputCode) {
        LinkCodeDetails details = codeCache.getIfPresent(inputCode);
        if (details == null) {
            return Optional.empty();
        }
        return Optional.of(new LinkCodeResult(details.provider(), details.providerId()));
    }

    /**
     * Принудительно удаляет код после успешного завершения транзакции в БД.
     */
    public void invalidateCode(String inputCode) {
        codeCache.invalidate(inputCode);
    }
}
