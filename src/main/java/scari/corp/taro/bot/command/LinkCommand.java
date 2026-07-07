package scari.corp.taro.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.facade.TaroBotFacade;

/**
 * Обработчик команд для генерации одноразовых кодов авторизации.
 * <p>
 * Генерирует защищенный 6-значный код для ввода в Личном Кабинете на сайте.
 */
@Component
@RequiredArgsConstructor
public class LinkCommand implements BotCommand {

    private final TaroBotFacade taroFacade;

    @Override
    public boolean canHandle(String input) {
        return "/link".equals(input) || StartCommand.BTN_LINK.equals(input);
    }

    @Override
    public BotResponse apply(String text, String destinationId, String username, String sessionId) {
        String code = taroFacade.generateLinkCodeForChat(BotProvider.TELEGRAM, destinationId);

        String messageText = "🔑 *Ваш код для привязки к сайту:* `" + code + "`\n\n" +
                "🤖 Чтобы объединить историю гаданий в боте и на сайте:\n" +
                "1. Зайдите на сайт под своим логином.\n" +
                "2. Перейдите в Личный кабинет -> Интеграции.\n" +
                "3. Введите этот 6-значный код.\n\n" +
                "⏱ _Код действителен в течение 5 минут и является одноразовым._";

        return BotResponse.builder()
                .destinationId(destinationId)
                .text(messageText)
                .build();
    }
}
