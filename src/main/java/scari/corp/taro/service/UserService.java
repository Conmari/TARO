package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.User;
import scari.corp.taro.entity.UserAccount;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.exception.AccountIntegrationException;
import scari.corp.taro.repository.TaroLayoutRepository;
import scari.corp.taro.repository.UserAccountRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.Optional;

/**
 * Сервис для управления профилями пользователей
 * и связывания внешних аккаунтов мессенджеров.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final TaroLayoutRepository taroLayoutRepository;
    private final UserRepository userRepository;

    /**
     * Находит пользователя сайта, к которому привязан чат мессенджера.
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByChatId(BotProvider provider, String providerId) {
        return userAccountRepository.findUserByAccountFields(provider.name(), providerId);
    }

    /**
     * Генератор сессий.
     */
    public String generateSessionId(BotProvider provider, String providerId) {
        return provider.name().toLowerCase() + "_" + providerId;
    }

    /**
     * Связывает аккаунт мессенджера с профилем пользователя сайта
     * и выполняет полное слияние накопленной гостевой истории гаданий.
     *
     * @param webUsername имя (логин) пользователя на основном веб-сайте
     * @param provider    тип подключаемой чат-платформы
     * @param providerId  идентификатор пользователя в мессенджере (chatId)
     * @throws IllegalArgumentException если указанный пользователь сайта не найден в БД
     * @throws IllegalStateException    если данный аккаунт мессенджера уже привязан к другой учетной записи
     */
    @Transactional
    public void linkAccountAndMergeHistory(String webUsername, BotProvider provider, String providerId) {
        User webUser = userRepository.findByUsername(webUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь сайта не найден: " + webUsername));

        if (userAccountRepository.existsByProviderAndProviderUserId(provider.name(), providerId)) {
            throw new AccountIntegrationException("Этот аккаунт " + provider + " уже привязан к другому профилю!");
        }

        if (userAccountRepository.existsByUserAndProvider(webUser, provider.name())) {
            throw new AccountIntegrationException("К вашему профилю уже привязан аккаунт " + provider + "!");
        }

        UserAccount newAccount = UserAccount.builder()
                .provider(provider.name())
                .providerUserId(providerId)
                .user(webUser)
                .build();
        userAccountRepository.save(newAccount);

        String guestSessionId = generateSessionId(provider, providerId);
        taroLayoutRepository.linkSessionToUser(webUser, guestSessionId);

        log.info("[UserService] Аккаунт {}:{} успешно привязан к пользователю '{}'. История сессии {} перенесена.",
                provider, providerId, webUsername, guestSessionId);
    }

    /**
     * Удаляет привязку внешнего аккаунта от профиля пользователя на сайте.
     * История раскладов при этом не удаляется (она остается закрепленной за пользователем сайта).
     *
     * @param webUsername имя пользователя на сайте
     * @param provider    тип отключаемой платформы
     * @throws AccountIntegrationException если у пользователя не было активного подключения к этой платформе
     */
    @Transactional
    public void unlinkAccount(String webUsername, BotProvider provider) {
        User webUser = userRepository.findByUsername(webUsername)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь сайта не найден: " + webUsername));

        int deletedRows = userAccountRepository.deleteByUserAndProvider(webUser, provider.name());

        if (deletedRows == 0) {
            throw new AccountIntegrationException("У вашего профиля нет активного подключения к " + provider.getTitle());
        }

        log.info("[UserService] От профиля '{}' успешно отключена платформа {}", webUsername, provider);
    }
}
