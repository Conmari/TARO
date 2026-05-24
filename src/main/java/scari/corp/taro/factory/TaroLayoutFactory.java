package scari.corp.taro.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.processor.TaroLayoutProcessor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaroLayoutFactory {
    private final Map<String, TaroLayoutProcessor> processors;

    public TaroLayoutProcessor getProcessor(LayoutType type) {
        TaroLayoutProcessor processor = processors.get(type.name());
        if (processor == null) {
            throw new IllegalArgumentException("Алгоритм для расклада " + type + " не найден");
        }
        return processor;
    }
}
