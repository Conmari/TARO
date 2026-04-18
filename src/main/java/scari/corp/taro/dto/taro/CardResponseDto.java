package scari.corp.taro.dto.taro;

import scari.corp.taro.entity.TaroCards;

/**
 * Используется вместо сущности {@link TaroCards}, для ответа с данными карты Таро.
 */
public record CardResponseDto(
        String nameRu,
        String arcana,
        String suit,
        Integer number,
        String upright,
        String reversed) {
}
