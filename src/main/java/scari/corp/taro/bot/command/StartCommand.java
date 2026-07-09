package scari.corp.taro.bot.command;

import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.enums.LayoutType;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик стартовой команды приветствия пользователя.
 * <p>
 * Перехватывает первичные маркеры запуска бота, формирует
 * приветственное сообщение и базовый набор экранных кнопок.
 */
@Component
public class StartCommand implements BotCommand {

    public static final String BTN_HISTORY = "📖 Моя история раскладов";
    public static final String BTN_LINK = "🔑 Привязать к сайту";

    @Override
    public boolean canHandle(String input) {
        return "/start".equals(input) || "начать".equalsIgnoreCase(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        String welcomeText = (username != null)
                ? "Рад видеть вас снова, <b>" + username + "</b>! ✨\nВыберите расклад:"
                : "Приветствую! Я бот Таро. 🔮\nВыберите расклад или привяжите аккаунт:";

        return BotResponse.builder()
                .destinationId(destinationId)
                .text(welcomeText)
                .buttons(buildMainMenu(username))
                .build();
    }

    /**
     * Метод генерации меню.
     * Возвращает правильную сетку кнопок в зависимости от того, авторизован юзер или нет.
     */
    public static List<List<String>> buildMainMenu(String username) {
        List<List<String>> gridButtons = new ArrayList<>();

        for (LayoutType type : LayoutType.values()) {
            gridButtons.add(List.of(type.getButtonText()));
        }

        if (username != null) {
            gridButtons.add(List.of(BTN_HISTORY));
        } else {
            gridButtons.add(List.of(BTN_HISTORY, BTN_LINK));
        }

        return gridButtons;
    }
}
