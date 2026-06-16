package scari.corp.taro.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import scari.corp.taro.processor.SelectedCard;

import java.util.List;

/**
 * Событие, публикуемое в системе после успешного создания новой структуры расклада.
 */
@Getter
public class TaroLayoutCreatedEvent extends ApplicationEvent {

    private final Long layoutId;
    private final List<SelectedCard> selectedCards;

    /**
     * Создает новый экземпляр события создания расклада.
     *
     * @param source        объект-инициатор события (обычно {@code this} вызывающего сервиса)
     * @param layoutId      уникальный идентификатор созданного родительского расклада
     * @param selectedCards список структур с вытянутыми картами
     */
    public TaroLayoutCreatedEvent(Object source, Long layoutId, List<SelectedCard> selectedCards) {
        super(source);
        this.layoutId = layoutId;
        this.selectedCards = selectedCards;
    }
}
