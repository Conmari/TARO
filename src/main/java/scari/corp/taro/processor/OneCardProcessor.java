package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.enums.LayoutType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Процессор для генерации расклада "Карта дня".
 * <p>
 * Реализует извлечение ровно одной карты напрямую через интерфейс {@link TaroLayoutProcessor}.
 * Алгоритм оптимизирован по скорости и памяти: вместо полного перемешивания колоды
 * он вычисляет один случайный индекс за время O(1) и определяет её пространственное положение.
 */
@Component
public class OneCardProcessor implements TaroLayoutProcessor {

    @Override
    public LayoutType getSupportedType() {
        return LayoutType.ONE_CARD;
    }

    @Override
    public List<SelectedCard> process(List<TaroCards> deck) {
        int randomIndex = ThreadLocalRandom.current().nextInt(deck.size());

        boolean isReversed = ThreadLocalRandom.current().nextBoolean();

        return List.of(new SelectedCard(deck.get(randomIndex), isReversed));
    }
}
