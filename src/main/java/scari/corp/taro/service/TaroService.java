package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroLayout;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.factory.TaroLayoutFactory;
import scari.corp.taro.mapper.TaroMapper;
import scari.corp.taro.processor.SelectedCard;
import scari.corp.taro.processor.TaroLayoutProcessor;
import scari.corp.taro.repository.TaroLayoutRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.List;

/**
 * Основной бизнес-сервис для управления логикой гаданий на картах Таро.
 * <p>
 * Координирует процессы загрузки колоды, выбора процессора расклада через фабрику,
 * создания агрегирующей сущности расклада и передачи карт на асинхронное фоновое сохранение.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCacheService taroCacheService;
    private final TaroLayoutRepository taroLayoutRepository;
    private final TaroHistoryAsyncService taroHistoryAsyncService;
    private final UserRepository userRepository;
    private final TaroMapper taroMapper;
    private final TaroLayoutFactory taroLayoutFactory;

    /**
     * Генерирует расклад карт Таро, сохраняет его структуру и инициирует асинхронную запись карт.
     * <p>
     * Метод работает по принципу минимизации задержек: синхронно создается только пустая
     * структура расклада {@link TaroLayout} для получения ID. Наполнение расклада картами
     * делегируется асинхронному сервису в фоновый пул потоков.
     *
     * @param username   имя авторизованного пользователя (null для гостей)
     * @param sessionId  идентификатор текущей гостевой веб-сессии
     * @param layoutType тип запрашиваемого расклада (например, ONE_CARD, THREE_CARDS)
     * @return список DTO {@link CardResponseDto} со всеми выпавшими картами и их толкованиями
     * @throws IllegalStateException если колода карт пуста или не инициализирована в кэше
     */
    @Transactional
    public List<CardResponseDto> generateLayout(String username, String sessionId, LayoutType layoutType) {
        List<TaroCards> allCards = taroCacheService.getAllCards();
        if (allCards.isEmpty()) throw new IllegalStateException("Колода пуста");

        TaroLayoutProcessor processor = taroLayoutFactory.getProcessor(layoutType);
        List<SelectedCard> selectedCards = processor.process(allCards);

        TaroLayout.TaroLayoutBuilder layoutBuilder = TaroLayout.builder()
                .layoutType(layoutType);

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + username));
            layoutBuilder.user(user);
        } else {
            layoutBuilder.sessionId(sessionId);
        }

        TaroLayout savedLayout = taroLayoutRepository.save(layoutBuilder.build());

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        taroHistoryAsyncService.saveLayoutCardsAsync(savedLayout.getId(), selectedCards);
                    }
                }
        );

        return selectedCards.stream()
                .map(item -> taroMapper.toCardResponseDto(item.card(), item.isReversed()))
                .toList();
    }

    /**
     * Возвращает страницу с последними сгенерированными раскладами пользователя или сессии.
     * <p>
     *
     * @param username  имя авторизованного пользователя (null для гостей)
     * @param sessionId идентификатор текущей гостевой веб-сессии
     * @param page      номер запрашиваемой страницы (начиная с 0)
     * @param size      количество раскладов на одной странице
     * @return страница Page с объектами {@link TaroHistoryResponseDto}
     */
    @Transactional(readOnly = true)
    public Page<TaroHistoryResponseDto> getLastReadings(String username, String sessionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaroLayout> layoutPage;

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь " + username + " не найден"));

            layoutPage = taroLayoutRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        } else {
            layoutPage = taroLayoutRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, pageable);
        }

        return layoutPage.map(taroMapper::toHistoryResponseDto);
    }
}
