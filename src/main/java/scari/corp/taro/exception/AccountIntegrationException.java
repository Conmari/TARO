package scari.corp.taro.exception;

/**
 * Исключение, выбрасываемое при нарушении бизнес-правил привязки аккаунтов.
 */
public class AccountIntegrationException extends RuntimeException {
    public AccountIntegrationException(String message) {
        super(message);
    }
}
