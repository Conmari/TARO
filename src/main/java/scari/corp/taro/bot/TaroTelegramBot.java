package scari.corp.taro.bot;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import scari.corp.taro.bot.command.BotCommand;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.facade.TaroBotFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Адаптер для интеграции с API Telegram .
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaroTelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final List<BotCommand> botCommands;
    private final TaroBotFacade taroFacade;

    @PostConstruct
    public void initLog() {
        log.info("🤖 Бот успешно инициализирован со свойствами: {}", botProperties.telegram().name());
    }

    @Override
    public String getBotUsername() {
        return this.botProperties.telegram().name();
    }

    @Override
    public String getBotToken() {
        return this.botProperties.telegram().token();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String input = update.getMessage().getText().trim();
        String destinationId = String.valueOf(update.getMessage().getChatId());

        log.info("📩 Получено сообщение в боте: '{}' от чата {}", input, destinationId);

        Optional<User> userOpt = taroFacade.findUserByChatId(BotProvider.TELEGRAM, destinationId);

        String username = userOpt.map(User::getUsername).orElse(null);
        String sessionId = userOpt.isPresent() ? null : taroFacade.generateSessionId(BotProvider.TELEGRAM, destinationId);

        BotResponse responseMessage = botCommands.stream()
                .filter(command -> command.canHandle(input))
                .findFirst()
                .map(command -> command.apply(input, destinationId, username, sessionId))
                .orElse(null);

        if (responseMessage == null) {
            responseMessage = BotResponse.builder()
                    .destinationId(destinationId)
                    .text("Неизвестная команда. Пожалуйста, используйте кнопки меню.")
                    .build();
        }

        try {
            SendMessage sendMessage = convertToTelegramMessage(responseMessage);
            execute(sendMessage);
            log.info("📤 Ответ успешно отправлен в Телеграм чат {}", destinationId);
        } catch (TelegramApiException e) {
            log.error("❌ Ошибка отправки сообщения через execute() в чат {}: {}", destinationId, e.getMessage());
        }
    }

    /**
     * Метод для конвертации.
     * Изолирует логику отрисовки интерфейса Telegram от бизнес-логики команд.
     */
    private static SendMessage convertToTelegramMessage(BotResponse response) {
        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(response.destinationId())
                .text(response.text())
                .parseMode("HTML");

        if (response.buttons() != null && !response.buttons().isEmpty()) {
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow currentRow = new KeyboardRow();

            for (String btnText : response.buttons()) {
                if (currentRow.size() >= 2) {
                    keyboard.add(currentRow);
                    currentRow = new KeyboardRow();
                }
                currentRow.add(new KeyboardButton(btnText));
            }

            keyboard.add(currentRow);

            builder.replyMarkup(ReplyKeyboardMarkup.builder()
                    .keyboard(keyboard)
                    .resizeKeyboard(true)
                    .selective(true)
                    .build());
        }

        return builder.build();
    }
}
