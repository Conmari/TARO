package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.dto.UserDto;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.User;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.repository.TaroHistoryRepository;
import scari.corp.taro.repository.UserRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCacheService taroCacheService;
    private final TaroHistoryRepository readingRepository;
    private final UserRepository userRepository;

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
            saveHistoryForUser(card, user);
        } else {
            saveHistoryForSession(card, sessionId);
        }
        return toCardResponseDto(card);
    }

    private void saveHistoryForUser(TaroCards card, User user) {
        TaroHistory reading = new TaroHistory();
        reading.setLayoutType(LayoutType.ONE_CARD);
        reading.setCard(card);
        reading.setUser(user);
        readingRepository.save(reading);
    }

    private void saveHistoryForSession(TaroCards card, String sessionId) {
        TaroHistory history = new TaroHistory();
        history.setLayoutType(LayoutType.ONE_CARD);
        history.setCard(card);
        history.setSessionId(sessionId);
        readingRepository.save(history);
    }

    private CardResponseDto toCardResponseDto(TaroCards card) {
        return new CardResponseDto(
                card.getNameRu(),
                card.getArcana().name(),
                card.getSuit(),
                card.getNumber(),
                card.getMeanings().getUpright(),
                card.getMeanings().getReversed()
        );
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
            historyPage = readingRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        } else {
            historyPage = readingRepository.findBySessionIdOrderByCreatedAtDesc(sessionId, pageable);
        }

        return historyPage.map(this::toHistoryResponseDto);
    }

    private TaroHistoryResponseDto toHistoryResponseDto(TaroHistory history) {
        CardResponseDto cardDto = toCardResponseDto(history.getCard());
        UserDto userDto = null;
        if (history.getUser() != null) {
            userDto = new UserDto(history.getUser().getId(), history.getUser().getUsername());
        }
        return new TaroHistoryResponseDto(
                history.getId(),
                history.getLayoutType(),
                cardDto,
                history.getCreatedAt(),
                userDto
        );
    }
}
