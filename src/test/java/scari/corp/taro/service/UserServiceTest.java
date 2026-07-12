package scari.corp.taro.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import scari.corp.taro.entity.User;
import scari.corp.taro.entity.UserAccount;
import scari.corp.taro.enums.BotProvider;
import scari.corp.taro.exception.AccountIntegrationException;
import scari.corp.taro.repository.TaroLayoutRepository;
import scari.corp.taro.repository.UserAccountRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private TaroLayoutRepository taroLayoutRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final String TEST_USERNAME = "dmitry_user";
    private final String TEST_CHAT_ID = "987654321";
    private final String EXPECTED_SESSION_ID = "telegram_987654321";
    private final String TELEGRAM = BotProvider.TELEGRAM.name();

    @Nested
    @DisplayName("Метод: linkAccountAndMergeHistory")
    class LinkAccountAndMergeHistoryTests {

        @Test
        @DisplayName("Успешная привязка Telegram-аккаунта и перенос гостевой истории раскладов")
        void shouldLinkAccountAndMergeHistorySuccessfully() {
            User mockUser = new User();
            mockUser.setUsername(TEST_USERNAME);

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userAccountRepository.existsByProviderAndProviderUserId(TELEGRAM, TEST_CHAT_ID)).thenReturn(false);
            when(userAccountRepository.existsByUserAndProvider(mockUser, TELEGRAM)).thenReturn(false);

            userService.linkAccountAndMergeHistory(TEST_USERNAME, BotProvider.TELEGRAM, TEST_CHAT_ID);

            ArgumentCaptor<UserAccount> accountCaptor = ArgumentCaptor.forClass(UserAccount.class);
            verify(userAccountRepository, times(1)).save(accountCaptor.capture());

            UserAccount savedAccount = accountCaptor.getValue();
            assertEquals(TELEGRAM, savedAccount.getProvider());
            assertEquals(TEST_CHAT_ID, savedAccount.getProviderUserId());
            assertEquals(mockUser, savedAccount.getUser());

            verify(taroLayoutRepository, times(1)).linkSessionToUser(mockUser, EXPECTED_SESSION_ID);
        }

        @Test
        @DisplayName("Ошибка: Пользователь сайта не найден в базе данных")
        void shouldThrowException_WhenWebUserNotFound() {
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    userService.linkAccountAndMergeHistory(TEST_USERNAME, BotProvider.TELEGRAM, TEST_CHAT_ID)
            );

            assertTrue(exception.getMessage().contains("Пользователь сайта не найден"));

            verify(userAccountRepository, never()).save(any());
            verify(taroLayoutRepository, never()).linkSessionToUser(any(), any());
        }

        @Test
        @DisplayName("Ошибка: Этот аккаунт Telegram уже привязан к другому профилю")
        void shouldThrowException_WhenTelegramAlreadyLinkedToSomeoneElse() {
            User mockUser = new User();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userAccountRepository.existsByProviderAndProviderUserId(TELEGRAM, TEST_CHAT_ID)).thenReturn(true);

            AccountIntegrationException exception = assertThrows(AccountIntegrationException.class, () ->
                    userService.linkAccountAndMergeHistory(TEST_USERNAME, BotProvider.TELEGRAM, TEST_CHAT_ID)
            );

            assertEquals("Этот аккаунт Telegram уже привязан к другому профилю!", exception.getMessage());

            verify(userAccountRepository, never()).save(any());
            verify(taroLayoutRepository, never()).linkSessionToUser(any(), any());
        }

        @Test
        @DisplayName("Ошибка: К текущему профилю сайта уже подключен другой Telegram")
        void shouldThrowException_WhenWebUserAlreadyHasTelegramLinked() {
            User mockUser = new User();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userAccountRepository.existsByProviderAndProviderUserId(TELEGRAM, TEST_CHAT_ID)).thenReturn(false);
            when(userAccountRepository.existsByUserAndProvider(mockUser, TELEGRAM)).thenReturn(true);

            AccountIntegrationException exception = assertThrows(AccountIntegrationException.class, () ->
                    userService.linkAccountAndMergeHistory(TEST_USERNAME, BotProvider.TELEGRAM, TEST_CHAT_ID)
            );

            assertEquals("К вашему профилю уже привязан аккаунт Telegram!", exception.getMessage());

            verify(userAccountRepository, never()).save(any());
            verify(taroLayoutRepository, never()).linkSessionToUser(any(), any());
        }
    }

    @Nested
    @DisplayName("Метод: unlinkAccount")
    class UnlinkAccountTests {

        @Test
        @DisplayName("Успешное удаление привязки Telegram-аккаунта")
        void shouldUnlinkAccountSuccessfully() {
            User mockUser = new User();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userAccountRepository.deleteByUserAndProvider(mockUser, TELEGRAM)).thenReturn(1);

            userService.unlinkAccount(TEST_USERNAME, BotProvider.TELEGRAM);

            verify(userAccountRepository, times(1)).deleteByUserAndProvider(mockUser, TELEGRAM);
        }

        @Test
        @DisplayName("Ошибка удаления: У пользователя не было активного подключения")
        void shouldThrowException_WhenNoActiveConnectionToUnlink() {
            User mockUser = new User();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userAccountRepository.deleteByUserAndProvider(mockUser, TELEGRAM)).thenReturn(0);

            AccountIntegrationException exception = assertThrows(AccountIntegrationException.class, () ->
                    userService.unlinkAccount(TEST_USERNAME, BotProvider.TELEGRAM)
            );

            assertEquals("У вашего профиля нет активного подключения к Telegram", exception.getMessage());
        }
    }
}
