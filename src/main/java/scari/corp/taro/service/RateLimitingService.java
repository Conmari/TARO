package scari.corp.taro.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitingService {

    // Кэш для карт Таро: запись удаляется, если пользователь не заходил 15 минут
    private final Cache<String, Bucket> taroCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    // Кэш для авторизации: запись удаляется через 15 минут
    private final Cache<String, Bucket> authCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(15))
            .build();

    /**
     * Проверяет лимит для раскладов Таро (1 запрос в 3 секунды).
     */
    public boolean tryConsume(String key) {
        Bucket bucket = taroCache.get(key, _ -> createTaroBucket());
        return bucket != null && bucket.tryConsume(1);
    }

    /**
     * Проверяет лимит для входа/брутфорса (5 попыток в минуту).
     */
    public boolean tryConsumeAuth(String key) {
        Bucket bucket = authCache.get(key, _ -> createAuthBucket());
        return bucket != null && bucket.tryConsume(1);
    }

    private Bucket createTaroBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(1)
                        .refillIntervally(1, Duration.ofSeconds(3)) // Добавлять по 1 токену каждые 3 секунды
                        .build())
                .build();
    }

    private Bucket createAuthBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1)) // Восстанавливает 5 попыток раз в минуту
                        .build())
                .build();
    }
}
