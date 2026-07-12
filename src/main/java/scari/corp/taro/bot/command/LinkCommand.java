package scari.corp.taro.bot.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import scari.corp.taro.bot.dto.BotResponse;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.facade.TaroBotFacade;

/**
 * Команда генерации одноразовых кодов авторизации.
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

        String messageText = """
                🔑 <b>Ваш код для привязки к сайту:</b> <code>%s</code>
                
                🤖 Чтобы объединить историю гаданий в боте и на сайте:
                1. Зайдите на сайт под своим логином.
                2. Введите этот 6-значный код в поле для интересующей платформы.
                
                ⏱ <i>Код действителен в течение 5 минут и является одноразовым.</i>
                """.formatted(code);

        return BotResponse.builder()
                .destinationId(destinationId)
                .text(messageText)
                .build();
    }
}
