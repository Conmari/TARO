package scari.corp.taro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import scari.corp.taro.dto.taro.CardResponseDto;
import scari.corp.taro.dto.taro.TaroHistoryResponseDto;
import scari.corp.taro.entity.TaroCards;
import scari.corp.taro.entity.TaroHistory;

    @Mapper(componentModel = "spring")
    public interface TaroMapper {

        @Mapping(target = "upright", source = "meanings.upright")
        @Mapping(target = "reversed", source = "meanings.reversed")
        CardResponseDto toCardResponseDto(TaroCards card);

        TaroHistoryResponseDto toHistoryResponseDto(TaroHistory history);
    }
