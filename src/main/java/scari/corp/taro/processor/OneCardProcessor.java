package scari.corp.taro.processor;

import org.springframework.stereotype.Component;
import scari.corp.taro.entity.TaroCards;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Процессор для вытягивания одной случайной карты Таро.
 * <p>
 * Реализует стратегию "Карта дня". Алгоритм оптимизирован по скорости и памяти:
 * вместо перемешивания всей колоды он вычисляет один случайный индекс за время O(1)
 * и подкидывает виртуальную монетку для определения пространственного положения карты.
 */
@Component("ONE_CARD")
public class OneCardProcessor implements TaroLayoutProcessor {

    /**
     * Извлекает одну случайную карту из переданной колоды.
     *
     * @param deck полная колода карт из кэш-слоя приложения
     * @return список, содержащий ровно один иммутабельный контейнер {@link SelectedCard}
     */
    @Override
    public List<SelectedCard> process(List<TaroCards> deck) {
        int randomIndex = ThreadLocalRandom.current().nextInt(deck.size());

        boolean isReversed = ThreadLocalRandom.current().nextBoolean();

        return List.of(new SelectedCard(deck.get(randomIndex), isReversed));
    }
}
