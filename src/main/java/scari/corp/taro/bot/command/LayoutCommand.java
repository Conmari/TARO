package scari.corp.taro.bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.dto.taro.CardResponseDto;
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
        return LayoutType.fromButtonText(input).isPresent();
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        LayoutType layoutType = LayoutType.fromButtonText(text).orElseThrow();

        try {
            List<CardResponseDto> layoutCards = taroFacade.executeLayoutDynamic(username, sessionId, layoutType);

            StringBuilder responseText = new StringBuilder(buildHeader(layoutType));

            for (int i = 0; i < layoutCards.size(); i++) {
                String cardText = buildCardItemText(i + 1, layoutCards.get(i));
                responseText.append(cardText);
            }

            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text(responseText.toString())
                    .buttons(StartCommand.buildMainMenu(username))
                    .build();

        } catch (Exception e) {
            log.error("Ошибка генерации расклада в Telegram для чата {}", destinationId, e);
            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text("⚠️ Не удалось построить расклад. Пожалуйста, попробуйте еще раз.")
                    .buttons(StartCommand.buildMainMenu(username))
                    .build();
        }
    }

    private static String buildHeader(LayoutType layoutType) {
        return "✨ <b>Ваш расклад готов: </b> <code>%s</code> ✨\n\n"
                .formatted(layoutType.getTitle());
    }

    private static String buildCardItemText(int position, CardResponseDto card) {
        String positionType = card.isReversed() ? "🙃 Перевернутое" : "➡️ Прямое";

        return """
                • <b>Позиция %d: </b> <code>%s</code>
                  Положение: %s
                  📖 <b>Толкование:</b> <i>%s</i>
                
                """.formatted(
                position,
                card.nameRu(),
                positionType,
                card.interpretation()
        );
    }
}
