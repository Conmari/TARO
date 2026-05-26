package scari.corp.taro.dto.taro;

import scari.corp.taro.entity.TaroCards;

/**
 * DTO для отправки данных о конкретной карте Таро клиенту.
 * <p>
 * Используется вместо тяжелой сущности {@link TaroCards}. Поля толкования динамически
 * адаптируются на бэкенде в зависимости от того, выпала карта в прямом или перевёрнутом положении.
 *
 * @param nameRu         название карты на русском языке (например, "Маг")
 * @param arcana         тип аркана (MAJOR или MINOR)
 * @param suit           масть карты (для младших арканов, например, "Кубки")
 * @param number         порядковый номер карты в колоде (от 0 до 21 или от 1 до 14)
 * @param isReversed     флаг пространственного положения карты: {@code true} — перевёрнутая, {@code false} — прямая
 * @param interpretation итоговый текст предсказания, выбранный на основе положения карты
 */
public record CardResponseDto(
        String nameRu,
        String arcana,
        String suit,
        Integer number,
        boolean isReversed,
        String interpretation
) {}
