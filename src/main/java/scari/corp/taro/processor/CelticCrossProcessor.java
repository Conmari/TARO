package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.enums.LayoutType;

/**
 * Процессор для генерации расклада "Кельтский крест".
 * <p>
 * Реализует сложную многопозиционную стратегию глубокого анализа ситуации из 10 карт.
 * Опирается на базовый алгоритм перемешивания {@link AbstractLayoutProcessor},
 * передавая в него фиксированное количество карт для последовательного извлечения.
 */
@Component
public class CelticCrossProcessor extends AbstractLayoutProcessor {

    @Override
    public LayoutType getSupportedType() {
        return LayoutType.CELTIC_CROSS;
    }

    @Override
    protected int getCardsCount() {
        return 10;
    }
}
