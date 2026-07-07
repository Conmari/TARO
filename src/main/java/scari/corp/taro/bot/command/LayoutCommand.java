package scari.corp.taro.bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.facade.TaroBotFacade;

import java.util.List;

/**
 * Обработчик команд для генерации новых раскладов карт Таро.
 * <p>
 * Перехватывает нажатия кнопок выбора раскладов
 * форматирует результат в текстовый ответ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LayoutCommand implements BotCommand {

    private final TaroBotFacade taroFacade;

    @Override
    public boolean canHandle(String input) {
        return StartCommand.BTN_ONE_CARD.equals(input) ||
                StartCommand.BTN_THREE_CARDS.equals(input) ||
                StartCommand.BTN_CELTIC_CROSS.equals(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        Long chatId = Long.parseLong(destinationId);

        LayoutType layoutType = LayoutType.ONE_CARD;
        if (StartCommand.BTN_THREE_CARDS.equals(text)) layoutType = LayoutType.THREE_CARDS;
        if (StartCommand.BTN_CELTIC_CROSS.equals(text)) layoutType = LayoutType.CELTIC_CROSS;

        try {
            List<CardResponseDto> layoutCards = taroFacade.executeLayoutDynamic(BotProvider.TELEGRAM, destinationId, layoutType);

            StringBuilder response = new StringBuilder();
            response.append("✨ *Ваш расклад готов: ").append(layoutType.name()).append("* ✨\n\n");

            for (int i = 0; i < layoutCards.size(); i++) {
                CardResponseDto card = layoutCards.get(i);
                response.append("• *Позиция ").append(i + 1).append(":* ")
                        .append(card.nameRu()).append("\n")
                        .append("  Положение: ").append(card.isReversed() ? "🙃 Перевернутое" : "➡️ Прямое").append("\n")
                        .append("  📖 *Толкование:* ").append(card.interpretation()).append("\n\n");
            }

            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text(response.toString())
                    .build();

        } catch (Exception e) {
            log.error("Ошибка генерации расклада в Telegram для чата {}", chatId, e);
            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text("⚠️ Не удалось построить расклад. Пожалуйста, попробуйте еще раз.")
                    .build();
        }
    }
}
