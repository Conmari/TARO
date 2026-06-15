package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.enums.LayoutType;

/**
 * Процессор для генерации расклада на три карты Таро.
 * <p>
 * Реализует классическую стратегию триады "Прошлое, Настоящее, Будущее".
 * Опирается на базовый алгоритм перемешивания {@link AbstractLayoutProcessor},
 * передавая в него фиксированное количество карт для извлечения.
 */
@Component
public class ThreeCardsProcessor extends AbstractLayoutProcessor {

    @Override
    public LayoutType getSupportedType() {
        return LayoutType.THREE_CARDS;
    }

    @Override
    protected int getCardsCount() {
        return 3;
    }
}
