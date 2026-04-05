package scari.corp.taro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import scari.corp.taro.dto.CardResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.repository.TaroCardsRepository;

@Service
@RequiredArgsConstructor
public class TaroService {

    private final TaroCardsRepository repository;

    /**
     * Возвращает случайную карту из колоды в виде DTO.
     *
     * @return {@link CardResponseDto} с данными карты
     */
    public CardResponseDto getRandomCard() {
        TaroCards card = repository.findRandomCard();
        if (card == null) {
            throw new IllegalStateException("Колода пуста");
        }
        return toCardResponseDto(card);

    }

    private CardResponseDto toCardResponseDto(TaroCards card) {
        return new CardResponseDto(
                card.getName(),
                card.getArcana().name(),
                card.getSuit(),
                card.getNumber(),
                card.getUprightMeaning(),
                card.getReversedMeaning()
        );
    }
}
