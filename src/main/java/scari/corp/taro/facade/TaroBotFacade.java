package scari.corp.taro.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.exception.AccountIntegrationException;
import scari.corp.taro.service.LinkCodeService;
import scari.corp.taro.service.TaroService;
import scari.corp.taro.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Фасад для интеграции внешних чат-платформ и веб-интерфейса системы.
 * <p>
 * Объединяя подсистемы управления пользователями, генерации раскладов и кэширования
 * одноразовых кодов верификации.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaroBotFacade {

    private final TaroService taroService;
    private final UserService userService;
    private final LinkCodeService linkCodeService;

    /**
     * Находит пользователя сайта, привязанного к чату мессенджера.
     */
    public Optional<User> findUserByChatId(BotProvider provider, String providerId) {
        return userService.findUserByChatId(provider, providerId);
    }

    /**
     * Генерирует строковый id гостевой сессии.
     */
    public String generateSessionId(BotProvider provider, String providerId) {
        return userService.generateSessionId(provider, providerId);
    }

    /**
     * Генерирует новый расклад карт Таро.
     * <p>
     * Принимает очищенные параметры сессии и пользователя напрямую от вызывающей стороны
     * и делегирует выполнение сервису бизнес-логики.
     *
     * @param username   имя авторизованного пользователя сайта (может быть {@code null} для гостей)
     * @param sessionId  идентификатор текущей гостевой или бот-сессии
     * @param layoutType тип запрашиваемого расклада карт
     * @return список DTO сгенерированных карт {@link CardResponseDto}
     */
    public List<CardResponseDto> executeLayoutDynamic(String username, String sessionId, LayoutType layoutType) {
        log.debug("[Фасад] Запрос расклада {}. Статус: {}",
                layoutType, (username != null ? "Авторизован (" + username + ")" : "Гость"));

        return taroService.generateLayout(username, sessionId, layoutType);
    }

    /**
     * Возвращает последние сохраненные расклады для конкретной сессии или профиля.
     * <p>
     * Метод скрывает логику пагинации репозитория и возвращает плоский список данных,
     * запрашивая всегда первую страницу истории.
     *
     * @param username  имя авторизованного пользователя сайта (может быть {@code null} для гостей)
     * @param sessionId текущей гостевой или бот-сессии
     * @param count     максимальное количество записей для извлечения
     * @return список DTO истории раскладов {@link TaroHistoryResponseDto}
     */
    public List<TaroHistoryResponseDto> getLatestReadingsDynamic(String username, String sessionId, int count) {
        Page<TaroHistoryResponseDto> page = taroService.getLastReadings(username, sessionId, 0, count);
        return page.getContent();
    }

    /**
     * Генерирует случайный криптостойкий одноразовый 6-значный код верификации для чата.
     * <p>
     * Используется на стороне мессенджера для инициации процесса связывания аккаунтов.
     * Гарантирует атомарную инвалидацию любых ранее выданных кодов для этого пользователя.
     *
     * @param provider   тип подключаемой чат-платформы
     * @param providerId уникальный строковый идентификатор пользователя в чате (chatId)
     * @return сгенерированная числовая строка одноразового кода привязки
     */
    public String generateLinkCodeForChat(BotProvider provider, String providerId) {
        return linkCodeService.generateCode(provider, providerId);
    }

    /**
     * Подтверждает привязку внешнего аккаунта к профилю на сайте по введенному коду.
     * <p>
     * <ol>
     *   <li>Выполняет мягкую проверку кода в кэше без его немедленного удаления.</li>
     *   <li>Вызывает изолированную транзакцию базы данных для создания связи и переноса истории.</li>
     *   <li>Только при успешном завершении транзакции окончательно удаляет код из кэша.</li>
     * </ol>
     *
     * @param webUsername имя авторизованного пользователя сайта, который инициировал привязку
     * @param inputCode   6-значный верификационный код, введенный пользователем на сайте
     * @return {@code true}, если интеграция успешно завершена;
     * {@code false}, если код неверный, истек по TTL или не существует
     */
    public boolean confirmLinkOnWebSite(String webUsername, String inputCode) {
        LinkCodeService.LinkCodeResult details = linkCodeService.verifyCodeWithoutConsuming(inputCode).orElse(null);

        if (details == null) {
            log.warn("[Фасад] Код неверен или истек: {}", inputCode);
            return false;
        }

        try {
            userService.linkAccountAndMergeHistory(webUsername, details.provider(), details.providerId());

            linkCodeService.invalidateCode(inputCode);

            return true;

        } catch (AccountIntegrationException e) {
            log.warn("[Фасад] Отмена привязки по правилам бизнес-логики: {}.", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[Фасад] Критическая системная ошибка привязки аккаунта.", e);
            throw e;
        }
    }

    /**
     * Удаляет привязку мессенджера от профиля на сайте.
     */
    public void unlinkAccountOnWebSite(String webUsername, BotProvider provider) {
        userService.unlinkAccount(webUsername, provider);
    }
}
