package scari.corp.taro.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Поддерживаемые чат-платформы.
 */
@Getter
@RequiredArgsConstructor
public enum BotProvider {
    TELEGRAM("Telegram");

    private final String title;
}
