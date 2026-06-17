package scari.corp.taro.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import scari.corp.taro.event.TaroLayoutCreatedEvent;
import scari.corp.taro.service.TaroHistoryService;

/**
 * EventListener для реактивной обработки созданных раскладов Таро.
 */
@Component
@RequiredArgsConstructor
public class TaroLayoutEventListener {

    private final TaroHistoryService taroHistoryService;

    /**
     * Перехватывает событие создания расклада и инициирует фоновое сохранение истории карт.
     * <p>
     * Выполнение переносится в асинхронный пул потоков (аннотация {@link Async})
     * и гарантированно стартует только после успешной фиксации (коммита)
     * основной транзакции бизнес-логики (фаза {@link TransactionPhase#AFTER_COMMIT}).
     *
     * @param event контейнер данных события с ID расклада и списком выбранных карт
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLayoutCreated(TaroLayoutCreatedEvent event) {
        taroHistoryService.saveLayoutCards(event.getLayoutId(), event.getSelectedCards());
    }
}
