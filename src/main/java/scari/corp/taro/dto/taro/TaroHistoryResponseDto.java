package scari.corp.taro.dto.taro;

import org.springframework.lang.Nullable;
import scari.corp.taro.dto.UserDto;
import scari.corp.taro.enums.LayoutType;

import java.time.LocalDateTime;

public record TaroHistoryResponseDto(Long id,
                                     LayoutType layoutType,
                                     CardResponseDto card,
                                     LocalDateTime createdAt,
                                     @Nullable UserDto user) {
}
