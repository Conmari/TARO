package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scari.corp.taro.dto.CardResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.enums.LayoutType;
import scari.corp.taro.repository.TaroCardsRepository;
import scari.corp.taro.repository.TaroHistoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCardsRepository repository;
    private final TaroHistoryRepository readingRepository;

    /**
     * Возвращает случайную карту из колоды в виде DTO.
     *
     * @return {@link CardResponseDto} с данными карты
     */
    @Transactional
    public CardResponseDto getRandomCard() {
        TaroCards card = repository.findRandomCard();
        if (card == null) {
            throw new IllegalStateException("Колода пуста");
        }

        TaroHistory reading = new TaroHistory();
        reading.setLayoutType(LayoutType.ONE_CARD);
        reading.setCard(card);
        readingRepository.save(reading);

        return toCardResponseDto(card);

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
    public List<TaroHistory> getLastReadings(int limit) {
        return readingRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .toList();
    }
}
