package scari.corp.taro.dto;

import scari.corp.taro.entity.TaroCards;

/**
 * Используется вместо сущности {@link TaroCards}, для ответа с данными карты Таро.
 */
public record CardResponseDto(
        String name,
        String arcana,
        String suit,
        Integer number,
        String uprightMeaning,
        String reversedMeaning) {
}
