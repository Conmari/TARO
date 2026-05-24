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
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.factory.TaroLayoutFactory;
import scari.corp.taro.mapper.TaroMapper;
import scari.corp.taro.processor.TaroLayoutProcessor;
import scari.corp.taro.repository.TaroHistoryRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCacheService taroCacheService;
    private final TaroHistoryRepository taroHistoryRepository;
    private final TaroHistoryAsyncService taroHistoryAsyncService;
    private final UserRepository userRepository;
    private final TaroMapper taroMapper;
    private final TaroLayoutFactory taroLayoutFactory;

    /**
     * Возвращает случайную карту из колоды в виде DTO.
     *
     * @return список карт {@link CardResponseDto} с данными карт
     */
    @Transactional
    public List<CardResponseDto> generateLayout(String username, String sessionId, LayoutType layoutType) {
        List<TaroCards> allCards = taroCacheService.getAllCards();
        if (allCards.isEmpty()) throw new IllegalStateException("Колода пуста");

        TaroLayoutProcessor processor = taroLayoutFactory.getProcessor(layoutType);
        List<TaroCards> selectedCards = processor.process(allCards);

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + username));

            for (TaroCards card : selectedCards) {
                taroHistoryAsyncService.saveHistoryForUserAsync(card.getId(), user.getId(), layoutType);
            }
        } else {
            for (TaroCards card : selectedCards) {
                taroHistoryAsyncService.saveHistoryForSessionAsync(card.getId(), sessionId, layoutType);
            }
        }

        return selectedCards.stream()
                .map(taroMapper::toCardResponseDto)
                .toList();
    }

    /**
     * Возвращает последние N гаданий.
     */
    @Transactional(readOnly = true)
    public Page<TaroHistoryResponseDto> getLastReadings(String username, String sessionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaroHistory> historyPage;

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь " + username + " не найден"));
            historyPage = taroHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        } else {
            historyPage = taroHistoryRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, pageable);
        }

        return historyPage.map(taroMapper::toHistoryResponseDto);
    }
}
