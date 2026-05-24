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
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.repository.TaroHistoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaroHistoryAsyncService {

    private final TaroHistoryRepository taroHistoryRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Асинхронно сохраняет историю расклада для зарегистрированного пользователя.
     */
    @Async
    @Transactional
    public void saveHistoryForUserAsync(Long cardId, Long userId, LayoutType layoutType) {
        TaroCards cardProxy = entityManager.getReference(TaroCards.class, cardId);
        User userProxy = entityManager.getReference(User.class, userId);

        TaroHistory history = TaroHistory.builder()
                .layoutType(layoutType)
                .card(cardProxy)
                .user(userProxy)
                .build();

        taroHistoryRepository.save(history);
    }

    /**
     * Асинхронно сохраняет историю расклада для гостевой сессии.
     */
    @Async
    @Transactional
    public void saveHistoryForSessionAsync(Long cardId, String sessionId, LayoutType layoutType) {
        TaroCards cardProxy = entityManager.getReference(TaroCards.class, cardId);

        TaroHistory history = TaroHistory.builder()
                .layoutType(layoutType)
                .card(cardProxy)
                .sessionId(sessionId)
                .build();

        taroHistoryRepository.save(history);
    }
}
