package scari.corp.taro.dto.auth;

/**
 * Универсальный DTO для отправки текстовых ответов от API.
 * <p>
 * Используется для стандартизации простых ответов сервера.
 *
 * @param message текстовое сообщение
 */
public record ApiResponse(String message) {
}
