package scari.corp.taro.bot.command;

import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;

import java.util.List;

/**
 * Обработчик стартовой команды приветствия пользователя.
 * <p>
 * Перехватывает первичные маркеры запуска бота, формирует
 * приветственное сообщение и базовый набор экранных кнопок.
 */
@Component
public class StartCommand implements BotCommand {

    public static final String BTN_ONE_CARD = "🔮 Карта дня (1 карта)";
    public static final String BTN_THREE_CARDS = "📜 Прошлое / Настоящее / Будущее (3 карты)";
    public static final String BTN_CELTIC_CROSS = "🎴 Кельтский крест (10 карт)";
    public static final String BTN_HISTORY = "📖 Моя история раскладов";
    public static final String BTN_LINK = "🔑 Привязать к сайту (/link)";

    @Override
    public boolean canHandle(String input) {
        return "/start".equals(input) || "начать".equalsIgnoreCase(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        return BotResponse.builder()
                .destinationId(destinationId)
                .text("Приветствую! Я персональный бот предсказаний Таро. 🔮\n\n" +
                        "Выберите нужный расклад или посмотрите историю на панели ниже:")
                .buttons(List.of(
                        BTN_ONE_CARD,
                        BTN_THREE_CARDS,
                        BTN_CELTIC_CROSS,
                        BTN_HISTORY,
                        BTN_LINK
                ))
                .build();
    }
}
