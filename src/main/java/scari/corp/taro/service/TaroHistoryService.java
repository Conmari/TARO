package scari.corp.taro.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.TaroLayout;
import scari.corp.taro.processor.SelectedCard;
import scari.corp.taro.repository.TaroHistoryRepository;

import java.util.List;

/**
 * Инфраструктурный сервис для сохранения истории выпавших карт в базу данных.
 * <p>
 * Выделяет логику работы с таблицей истории в изолированный компонент. Делегирует
 * управление потоками вызывающей стороне, что позволяет использовать его как
 * синхронно, так и внутри асинхронных слушателей событий (Event Listeners).
 */
@Service
@RequiredArgsConstructor
public class TaroHistoryService {

    private final TaroHistoryRepository taroHistoryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Выполняет пакетную запись вытянутых карт для конкретного расклада.
     * <p>
     * Алгоритм оптимизирован для минимизации нагрузки на БД: вместо выполнения тяжелых
     * запросов {@code SELECT} для проверки ассоциаций, он использует легковесные JPA-прокси
     * через {@link EntityManager#getReference}.
     * Порядковый номер карты автоматически фиксируется на основе её индекса в списке.
     *
     * @param layoutId      идентификатор созданного родительского расклада
     * @param selectedCards список контейнеров {@link SelectedCard} с картами и их положениями
     */
    @Transactional
    public void saveLayoutCards(Long layoutId, List<SelectedCard> selectedCards) {
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
