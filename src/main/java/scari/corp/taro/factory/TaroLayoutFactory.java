package scari.corp.taro.factory;

import org.springframework.stereotype.Component;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.processor.TaroLayoutProcessor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Фабрика компонентов для динамического управления процессорами раскладов Таро.
 * <p>
 * Обеспечивает централизованный доступ к стратегиям генерации раскладов.
 * При старте приложения автоматически собирает все доступные реализации
 * {@link TaroLayoutProcessor} в индексированную карту.
 */
@Component
public class TaroLayoutFactory {

    /**
     * Карта зарегистрированных процессоров раскладов.
     * Ключ — тип расклада {@link LayoutType},
     * значение — соответствующий класс-обработчик алгоритма.
     */
    private final Map<LayoutType, TaroLayoutProcessor> processors;

    /**
     * Конструктор фабрики для автоматического сбора процессоров Спрингом.
     *
     * @param processorList список всех обнаруженных в контексте бинов {@link TaroLayoutProcessor}
     */
    public TaroLayoutFactory(List<TaroLayoutProcessor> processorList) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(
                        TaroLayoutProcessor::getSupportedType,
                        processor -> processor
                ));
    }

    /**
     * Возвращает конкретную стратегию (процессор) в зависимости от выбранного типа расклада.
     *
     * @param type перечисление {@link LayoutType}, определяющее вид гадания (например, ONE_CARD, THREE_CARDS)
     * @return объект {@link TaroLayoutProcessor}, содержащий алгоритм выбора карт из колоды
     * @throws IllegalArgumentException если для переданного типа расклада не найдено зарегистрированного процессора
     */
    public TaroLayoutProcessor getProcessor(LayoutType type) {
        TaroLayoutProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("Алгоритм для расклада " + type + " не найден");
        }
        return processor;
    }
}
