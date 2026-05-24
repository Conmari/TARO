package scari.corp.taro.processor;

import scari.corp.taro.entity.TaroCards;

/**
 * Иммутабельный контейнер (Record), представляющий карту, выбранную в процессе расклада.
 * <p>
 * Используется в слое процессоров алгоритмов гадания для передачи пары значений:
 * самой сущности карты и её случайного положения (прямое или перевёрнутое).
 *
 * @param card       сущность вытянутой карты Таро из кэша
 * @param isReversed флаг положения карты: {@code true} — перевёрнутое значение,
 *                   {@code false} — прямое значение
 */
public record SelectedCard(TaroCards card, boolean isReversed) {
}
