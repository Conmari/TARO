package scari.corp.taro.bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.facade.TaroBotFacade;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Обработчик команд для запроса истории гаданий пользователя.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryCommand implements BotCommand {

    private final TaroBotFacade taroFacade;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public boolean canHandle(String input) {
        return "/history".equals(input) || StartCommand.BTN_HISTORY.equals(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        try {
            List<TaroHistoryResponseDto> history = taroFacade.getLatestReadingsDynamic(username, sessionId, 4);

            if (history.isEmpty()) {
                return BotResponse.builder()
                        .destinationId(destinationId)
                        .text("📜 *У вас пока нет сохраненной истории раскладов.*\n\n" +
                                "Сделайте свой первый расклад с помощью кнопок меню меню 👇")
                        .build();
            }

            StringBuilder response = new StringBuilder();
            response.append("📖 *Ваши последние расклады:*\n\n");

            for (TaroHistoryResponseDto reading : history) {
                response.append("🗓 *Расклад:* `").append(reading.layoutType().name()).append("`\n")
                        .append("📅 *Дата:* _").append(reading.createdAt().format(DATE_FORMATTER)).append("_\n");

                response.append("🎴 *Выпавшие карты:* ");
                List<String> cardNames = reading.cards().stream()
                        .map(card -> card.nameRu() + (card.isReversed() ? " 🔄 (обр.)" : " ⬆️"))
                        .toList();

                response.append(String.join(", ", cardNames)).append("\n");
                response.append("───────────────────\n\n");
            }

            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text(response.toString())
                    .build();

        } catch (Exception e) {
            log.error("Ошибка при получении истории гаданий для назначения {}: ", destinationId, e);
            return BotResponse.builder()
                    .destinationId(destinationId)
                    .text("⚠️ Не удалось загрузить историю раскладов. Пожалуйста, попробуйте позже.")
                    .build();
        }
    }
}
