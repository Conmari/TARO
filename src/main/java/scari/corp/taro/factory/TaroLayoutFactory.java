package scari.corp.taro.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.processor.TaroLayoutProcessor;

import java.util.Map;

/**
 * Фабрика компонентов для динамического управления процессорами раскладов Таро.
 * <p>
 * Реализует интерфейс {@link TaroLayoutProcessor}
 * и собирает их в карту {@code Map<String, TaroLayoutProcessor>}.
 */
@Component
@RequiredArgsConstructor
public class TaroLayoutFactory {

    /**
     * Карта зарегистрированных Спрингом процессоров раскладов.
     * Ключ — строковое представление {@link LayoutType},
     * значение — соответствующий класс-обработчик алгоритма перемешивания.
     */
    private final Map<String, TaroLayoutProcessor> processors;

    /**
     * Возвращает конкретную стратегию (процессор) генерации карт в зависимости от выбранного типа расклада.
     *
     * @param type перечисление {@link LayoutType}, определяющее вид гадания (например, ONE_CARD, THREE_CARDS)
     * @return объект {@link TaroLayoutProcessor}, содержащий математический алгоритм выбора карт из колоды
     * @throws IllegalArgumentException если для переданного типа расклада не найдено зарегистрированного бина-процессора
     */
    public TaroLayoutProcessor getProcessor(LayoutType type) {
        TaroLayoutProcessor processor = processors.get(type.name());
        if (processor == null) {
            throw new IllegalArgumentException("Алгоритм для расклада " + type + " не найден");
        }
        return processor;
    }
}
