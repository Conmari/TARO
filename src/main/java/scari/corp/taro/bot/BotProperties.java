package scari.corp.taro.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурационные свойства для управления чат-платформами.
 * <p>
 */
@ConfigurationProperties(prefix = "bots")
public record BotProperties(
        TelegramProperties telegram
) {
    /**
     * Конфигурационные параметры для бота Telegram.
     *
     * @param name  имя бота
     * @param token токен
     */
    public record TelegramProperties(
            String name,
            String token
    ) {}
}
