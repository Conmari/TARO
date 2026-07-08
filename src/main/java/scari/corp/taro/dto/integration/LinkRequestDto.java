package scari.corp.taro.dto.integration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Запрос на привязку аккаунта.
 */
public record LinkRequestDto(
        @NotBlank(message = "Код привязки не может быть пустым")
        @Pattern(regexp = "^\\d{6}$", message = "Код привязки должен состоять ровно из 6 цифр")
        String code
) {}
