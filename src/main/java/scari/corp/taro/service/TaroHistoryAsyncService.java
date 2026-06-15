package scari.corp.taro.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.TaroLayout;
import scari.corp.taro.processor.SelectedCard;
import scari.corp.taro.repository.TaroHistoryRepository;

import java.util.List;

/**
 * Асинхронный сервис для выполнения инфраструктурных задач по сохранению истории карт.
 * <p>
 * Изолирует тяжелые операции записи в базу данных от основного потока веб-сервера,
 * обеспечивая мгновенный ответ пользователю при генерации раскладов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaroHistoryAsyncService {

    private final TaroHistoryRepository taroHistoryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Асинхронно сохраняет состав карт для конкретного сеанса гадания в фоновом потоке.
     * <p>
     * Метод оптимизирован для минимизации нагрузки на СУБД: вместо выполнения тяжелых
     * запросов {@code SELECT} для проверки связей, он использует легковесные прокси-объекты
     * через {@link EntityManager#getReference(Class, Object)}.
     * Порядковый номер карты автоматически фиксируется на основе её индекса в списке.
     *
     * @param layoutId      уникальный идентификатор уже созданного родительского расклада
     * @param selectedCards список структур {@link SelectedCard} с вытянутыми картами и их положениями
     */
    @Async
    @Transactional
    public void saveLayoutCardsAsync(Long layoutId, List<SelectedCard> selectedCards) {
        TaroLayout layoutProxy = entityManager.getReference(TaroLayout.class, layoutId);

        for (int i = 0; i < selectedCards.size(); i++) {
            SelectedCard item = selectedCards.get(i);
            TaroCards cardProxy = entityManager.getReference(TaroCards.class, item.card().getId());

            TaroHistory history = TaroHistory.builder()
                    .layout(layoutProxy)
                    .card(cardProxy)
                    .isReversed(item.isReversed())
                    .cardOrder(i)
                    .build();

            taroHistoryRepository.save(history);
        }
    }
}
