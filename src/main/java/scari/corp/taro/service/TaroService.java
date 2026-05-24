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
import scari.corp.taro.mapper.TaroMapper;
import scari.corp.taro.repository.TaroHistoryRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCacheService taroCacheService;
    private final TaroHistoryRepository taroHistoryRepository;
    private final TaroHistoryAsyncService taroHistoryAsyncService;
    private final UserRepository userRepository;
    private final TaroMapper taroMapper;

    /**
     * Возвращает случайную карту из колоды в виде DTO.
     *
     * @return {@link CardResponseDto} с данными карты
     */
    @Transactional
    public CardResponseDto getRandomCard(String username, String sessionId) {
        List<TaroCards> allCards = taroCacheService.getAllCards();
        if (allCards.isEmpty()) throw new IllegalStateException("Колода пуста");

        int randomIndex = ThreadLocalRandom.current().nextInt(allCards.size());
        TaroCards card = allCards.get(randomIndex);

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Пользователь не найден: " + username));
            taroHistoryAsyncService.saveHistoryForUserAsync(card.getId(), user.getId(), LayoutType.ONE_CARD);
        } else {
            taroHistoryAsyncService.saveHistoryForSessionAsync(card.getId(), sessionId, LayoutType.ONE_CARD);
        }
        return taroMapper.toCardResponseDto(card);
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
