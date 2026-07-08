package scari.corp.taro.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Типы раскладов
 */
@Getter
@RequiredArgsConstructor
public enum LayoutType {
    ONE_CARD("Карта дня", "🔮 Карта дня (1 карта)"),
    THREE_CARDS("Расклад из 3 карт", "📜 Прошлое / Настоящее / Будущее (3 карты)"),
    CELTIC_CROSS("Кельтский крест (10 карт)", "🎴 Кельтский крест (10 карт)");

    private final String title;

    private final String buttonText;

    public static Optional<LayoutType> fromButtonText(String text) {
        for (LayoutType type : values()) {
            if (type.buttonText.equals(text)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
