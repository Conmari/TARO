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
    public static final String BTN_LINK = "🔑 Привязать к сайту (/link)";

    @Override
    public boolean canHandle(String input) {
        return "/start".equals(input) || "начать".equalsIgnoreCase(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        String welcomeText;
        List<String> activeButtons = new ArrayList<>();

        for (LayoutType type : LayoutType.values()) {
            activeButtons.add(type.getButtonText());
        }

        activeButtons.add(BTN_HISTORY);

        if (username != null) {
            welcomeText = "Рад видеть вас снова, <b>" + username + "</b>! ✨\nВыберите расклад:";
        } else {
            welcomeText = "Приветствую! Я бот Таро. 🔮\nВыберите расклад или привяжите аккаунт:";
            activeButtons.add(BTN_LINK);
        }

        return BotResponse.builder()
                .destinationId(destinationId)
                .text(welcomeText)
                .buttons(activeButtons)
                .build();
    }
}
