package scari.corp.taro.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import scari.corp.taro.bot.command.BotCommand;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.facade.TaroBotFacade;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Адаптер для интеграции с API Telegram .
 */
@Slf4j
@Component
public class TaroTelegramBot extends TelegramLongPollingBot {

    public static final String PATH_TARO_CARDS = "static/images/taro-cards/";

    private final BotProperties botProperties;
    private final List<BotCommand> botCommands;
    private final TaroBotFacade taroFacade;

    public TaroTelegramBot(BotProperties botProperties,
                           List<BotCommand> botCommands,
                           TaroBotFacade taroFacade) {
        super(botProperties.telegram().token());

        this.botProperties = botProperties;
        this.botCommands = botCommands;
        this.taroFacade = taroFacade;

        log.info("[Telegram] Бот {} успешно инициализирован", botProperties.telegram().name());
    }

    @Override
    public String getBotUsername() {
        return this.botProperties.telegram().name();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String input = update.getMessage().getText().trim();
        String destinationId = String.valueOf(update.getMessage().getChatId());

        log.debug("📩 Получено сообщение в боте: '{}' от чата {}", input, destinationId);

        Optional<User> userOpt = taroFacade.findUserByChatId(BotProvider.TELEGRAM, destinationId);
        String username = userOpt.map(User::getUsername).orElse(null);
        String sessionId = userOpt.isPresent() ? null : taroFacade.generateSessionId(BotProvider.TELEGRAM, destinationId);

        BotResponse responseMessage = botCommands.stream()
                .filter(command -> command.canHandle(input))
                .findFirst()
                .map(command -> command.apply(input, destinationId, username, sessionId))
                .orElseGet(() -> BotResponse.builder()
                        .destinationId(destinationId)
                        .text("Неизвестная команда. Пожалуйста, используйте кнопки меню.")
                        .build());

        ReplyKeyboard fallbackMarkup = null;

        try {
            SendMessage sendMessage = convertToTelegramMessage(responseMessage);
            if (sendMessage != null) {
                fallbackMarkup = sendMessage.getReplyMarkup();
            }

            List<String> urls = responseMessage.imageUrls();
            boolean isLayout = urls != null && !urls.isEmpty();

            if (isLayout) {
                sendPlaceholderMessage(destinationId);
            }

            if (isLayout) {
                if (urls.size() == 1) {
                    sendSinglePhoto(destinationId, urls.getFirst());
                } else {
                    sendAlbumPhoto(destinationId, urls);
                }
            }

            execute(sendMessage);
        } catch (Exception e) {
            log.error("[Telegram] Ошибка отправки в чат {}: {}", destinationId, e.getMessage(), e);
            sendErrorFallback(destinationId, fallbackMarkup);
        }
    }

    private void sendErrorFallback(String chatId, ReplyKeyboard replyMarkup) {
        try {
            var errorFallback = SendMessage.builder()
                    .chatId(chatId)
                    .text("""
                            ⚠️ <b>Произошла ошибка при загрузке карт или отправке ответа.</b>
                            Пожалуйста, попробуйте сделать расклад еще раз чуть позже.
                            """)
                    .parseMode("HTML")
                    .replyMarkup(replyMarkup)
                    .build();
            execute(errorFallback);
        } catch (TelegramApiException fallbackException) {
            log.error("[Telegram] Не удалось отправить даже сообщение об ошибке в чат {}", chatId, fallbackException);
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

            for (List<String> rowButtons : response.buttons()) {
                KeyboardRow currentRow = new KeyboardRow();
                for (String btnText : rowButtons) {
                    currentRow.add(new KeyboardButton(btnText));
                }
                keyboard.add(currentRow);
            }

            builder.replyMarkup(ReplyKeyboardMarkup.builder()
                    .keyboard(keyboard)
                    .resizeKeyboard(true)
                    .selective(true)
                    .build());
        }

        return builder.build();
    }

    /**
     * Отправляет одиночную картинку карты Таро.
     */
    private void sendSinglePhoto(String chatId, String imageKey) throws TelegramApiException {
        String path = PATH_TARO_CARDS + imageKey;
        ClassPathResource resource = new ClassPathResource(path);

        try (InputStream is = resource.getInputStream()) {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(is, imageKey))
                    .build();

            execute(sendPhoto);
            log.debug("[Telegram] Одиночная карта {} успешно отправлена из resources в чат {}", imageKey, chatId);
        } catch (Exception e) {
            log.error("[Telegram] Не удалось прочитать картинку {} из папки resources: {}", imageKey, e.getMessage());
            throw new TelegramApiException("Ошибка чтения файла изображения", e);
        }
    }

    /**
     * Отправляет пакет картинок в виде единого альбома.
     */
    private void sendAlbumPhoto(String chatId, List<String> imageKeys) throws TelegramApiException {
        List<InputMedia> mediaGroup = new ArrayList<>();
        List<InputStream> openStreams = new ArrayList<>();

        try {
            for (String key : imageKeys) {
                String path = PATH_TARO_CARDS + key;
                ClassPathResource resource = new ClassPathResource(path);
                InputStream is = resource.getInputStream();
                openStreams.add(is);

                InputMediaPhoto photo = new InputMediaPhoto();
                photo.setMedia(is, key);
                mediaGroup.add(photo);
            }

            SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                    .chatId(chatId)
                    .medias(mediaGroup)
                    .build();

            execute(sendMediaGroup);
            log.debug("[Telegram] Альбом из {} карт успешно отправлен из resources в чат {}", mediaGroup.size(), chatId);
        } catch (Exception e) {
            log.error("[Telegram] Ошибка сборки альбома карт из папки resources в чат {}: {}", chatId, e.getMessage());
            throw new TelegramApiException("Ошибка отправки альбома изображений", e);
        } finally {
            for (InputStream is : openStreams) {
                try {
                    if (is != null) is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Отправляет сообщение о начале генерации расклада.
     */
    private void sendPlaceholderMessage(String chatId) throws TelegramApiException {
        SendMessage placeholder = SendMessage.builder()
                .chatId(chatId)
                .text("✨ <i>Перемешиваем колоду и открываем тайны...</i>")
                .parseMode("HTML")
                .build();

        execute(placeholder);
    }
}
