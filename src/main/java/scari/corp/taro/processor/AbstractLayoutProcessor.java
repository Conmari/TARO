package scari.corp.taro.processor;

import scari.corp.taro.entity.TaroCards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Базовый абстрактный класс для процессоров раскладов Таро.
 * <p>
 * Реализует стандартный алгоритм перемешивания всей колоды карт
 * и случайного определения их пространственного положения (прямое/перевёрнутое).
 * Количество карт для каждого конкретного расклада делегируется
 * классам-наследникам через метод {@link #getCardsCount()}.
 */
public abstract class AbstractLayoutProcessor implements TaroLayoutProcessor {

    @Override
    public List<SelectedCard> process(List<TaroCards> deck) {
        List<TaroCards> shuffledDeck = new ArrayList<>(deck);

        Collections.shuffle(shuffledDeck);

        int count = getCardsCount();
        List<SelectedCard> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            boolean isReversed = ThreadLocalRandom.current().nextBoolean();
            result.add(new SelectedCard(shuffledDeck.get(i), isReversed));
        }
        return result;
    }

    /**
     * Возвращает количество карт, необходимое для конкретного расклада.
     * Реализуется каждым типом гадания индивидуально.
     *
     * @return количество карт
     */
    protected abstract int getCardsCount();
}
