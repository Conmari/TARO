package scari.corp.taro.processor;

import scari.corp.taro.entity.TaroCards;

import java.util.List;

/**
 * Интерфейс процессора (обработчика) для вычисления раскладов Таро.
 */
public interface TaroLayoutProcessor {
    /**
     * Выполняет алгоритм расклада: выбирает нужное количество случайных карт из колоды.
     *
     * @param deck полная колода карт из кэша
     * @return список выбранных карт для расклада
     */
    List<SelectedCard> process(List<TaroCards> deck);
}
