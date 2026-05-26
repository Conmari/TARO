package scari.corp.taro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;
import scari.corp.taro.entity.TaroLayout;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TaroMapper {

    @Mapping(target = "cards", source = "layout")
    TaroHistoryResponseDto toHistoryResponseDto(TaroLayout layout);

    /**
     * Превращает список сущностей TaroHistory в отсортированный список DTO карт.
     */
    default List<CardResponseDto> mapCards(TaroLayout layout) {
        if (layout == null || layout.getCards() == null) return List.of();

        return layout.getCards().stream()
                .sorted(Comparator.comparingInt(TaroHistory::getCardOrder))
                .map(history -> toCardResponseDto(history.getCard(), history.isReversed()))
                .toList();
    }

    default CardResponseDto toCardResponseDto(TaroCards card, boolean isReversed) {
        if (card == null) return null;
        String text = isReversed ? card.getMeanings().getReversed() : card.getMeanings().getUpright();
        return new CardResponseDto(
                card.getNameRu(),
                card.getArcana() != null ? card.getArcana().name() : null,
                card.getSuit(),
                card.getNumber(),
                isReversed,
                text
        );
    }
}
