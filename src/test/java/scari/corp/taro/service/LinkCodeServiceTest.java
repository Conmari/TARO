package scari.corp.taro.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scari.corp.taro.enums.BotProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LinkCodeService")
class LinkCodeServiceTest {

    public static final String CHAT_ID = "555666777";

    private final LinkCodeService linkCodeService = new LinkCodeService();

    @Test
    @DisplayName("Успешная генерация кода и его последующая валидация")
    void shouldGenerateAndVerifyCodeSuccessfully() {
        String chatId = CHAT_ID;

        String code = linkCodeService.generateCode(BotProvider.TELEGRAM, chatId);

        assertNotNull(code);
        assertTrue(code.matches("^\\d{6}$"));

        Optional<LinkCodeService.LinkCodeResult> resultOpt = linkCodeService.verifyCodeWithoutConsuming(code);
        assertTrue(resultOpt.isPresent());

        LinkCodeService.LinkCodeResult result = resultOpt.get();
        assertEquals(BotProvider.TELEGRAM, result.provider());
        assertEquals(chatId, result.providerId());
    }

    @Test
    @DisplayName("Старый код должен сгорать при генерации нового кода для того же чата")
    void shouldInvalidateOldCode_WhenNewCodeGeneratedForSameChat() {
        String chatId = CHAT_ID;

        String firstCode = linkCodeService.generateCode(BotProvider.TELEGRAM, chatId);
        String secondCode = linkCodeService.generateCode(BotProvider.TELEGRAM, chatId);

        Optional<LinkCodeService.LinkCodeResult> firstResult = linkCodeService.verifyCodeWithoutConsuming(firstCode);
        assertTrue(firstResult.isEmpty());

        Optional<LinkCodeService.LinkCodeResult> secondResult = linkCodeService.verifyCodeWithoutConsuming(secondCode);
        assertTrue(secondResult.isPresent());
    }
}
