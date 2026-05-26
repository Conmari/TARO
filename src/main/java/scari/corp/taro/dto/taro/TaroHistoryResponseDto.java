package scari.corp.taro.dto.taro;

import org.springframework.lang.Nullable;
import scari.corp.taro.dto.UserDto;
import scari.corp.taro.enums.LayoutType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для отображения расклада Таро в истории гаданий.
 * <p>
 * Объединяет общие метаданные сессии гадания (тип расклада, дата, пользователь)
 * и упорядоченный список всех карт, которые выпали в рамках этого конкретного сеанса.
 *
 * @param id         уникальный идентификатор сохраненного расклада
 * @param layoutType тип выполненного расклада (например, ONE_CARD, THREE_CARDS)
 * @param createdAt  точная дата и время проведения сеанса гадания
 * @param user       DTO {@link UserDto} с данными пользователя, если расклад был выполнен авторизованным аккаунтом
 * @param cards      упорядоченный по позициям список DTO {@link CardResponseDto} карт, выпавших в раскладе
 */
public record TaroHistoryResponseDto(
        Long id,
        LayoutType layoutType,
        LocalDateTime createdAt,
        @Nullable UserDto user,
        List<CardResponseDto> cards
) {}
